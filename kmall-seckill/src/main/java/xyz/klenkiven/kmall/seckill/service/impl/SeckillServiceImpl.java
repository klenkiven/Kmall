package xyz.klenkiven.kmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.klenkiven.kmall.common.to.SkuInfoTO;
import xyz.klenkiven.kmall.common.to.UserLoginTO;
import xyz.klenkiven.kmall.common.to.mq.SeckillOrderTO;
import xyz.klenkiven.kmall.common.utils.R;
import xyz.klenkiven.kmall.seckill.feign.CouponFeignService;
import xyz.klenkiven.kmall.seckill.feign.ProductFeignService;
import xyz.klenkiven.kmall.seckill.interceptor.UserLoginInterceptor;
import xyz.klenkiven.kmall.seckill.model.dto.SeckillSessionDTO;
import xyz.klenkiven.kmall.common.to.SeckillSkuRedisTO;
import xyz.klenkiven.kmall.seckill.service.SeckillService;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private static final String SECKILL_SKU_CACHE = "seckill:skus";
    private static final String SKU_STOCK_CACHE_PREFIX = "seckill:stock:";
    private static final String SECKILL_USER_PREFIX = "seckill:user:";

    private final CouponFeignService couponFeignService;
    private final ProductFeignService productFeignService;
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private final RedissonClient redissonClient;

    @Override
    public void uploadSeckillSkuLatest3Day() {
        // SCAN SECKILL SKU
        List<SeckillSessionDTO> seckillSessions = couponFeignService.latest3DaysSession().getData();
        if (seckillSessions == null) {
            return;
        }

        // UPLOAD SKU
        // Save Session Info to Redis
        saveSessionInfo(seckillSessions);
        // Save SKU in Session to Redis
        saveSessionSku(seckillSessions);
    }

    @Override
    public List<SeckillSkuRedisTO> getCurrentSeckillSkus() {
        // Determine which seckill session
        long currentTime = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");

        // Traverse all of session
        List<SeckillSkuRedisTO> result = new ArrayList<>();
        if (keys == null || keys.size() <= 0) {
            return new ArrayList<>();
        }
        keys.forEach(key -> {
            String sessionTime = key.replace(SESSION_CACHE_PREFIX, "");
            String[] times = sessionTime.split("_");
            long startTime = Long.parseLong(times[0]);
            long endTime = Long.parseLong(times[1]);
            if (startTime <= currentTime && currentTime <= endTime) {
                // Get Skus
                BoundHashOperations<String, String, Object> hashOps =
                        redisTemplate.boundHashOps(SECKILL_SKU_CACHE);
                List<String> sessionSkuIdList = redisTemplate.opsForList().range(key, -100, 100);
                List<Object> list = hashOps.multiGet(sessionSkuIdList);
                if (list != null) {
                    List<SeckillSkuRedisTO> collect = list.stream()
                            .map(item -> {
                                SeckillSkuRedisTO redisTO = new SeckillSkuRedisTO();
                                SeckillSkuRedisTO source = JSON.parseObject((String) item, SeckillSkuRedisTO.class);
                                BeanUtils.copyProperties(source, redisTO);
                                return redisTO;
                            }).collect(Collectors.toList());
                    result.addAll(collect);
                }
            }
        });
        return result;
    }

    @Override
    public SeckillSkuRedisTO getSkuSeckill(Long skuID) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKU_CACHE);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                String regx = "\\d_" + skuID;
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SeckillSkuRedisTO seckillSkuRedis = JSON.parseObject(json, SeckillSkuRedisTO.class);
                    long currentTime = new Date().getTime();
                    if (seckillSkuRedis != null &&
                            seckillSkuRedis.getStartTime() < currentTime &&
                            currentTime < seckillSkuRedis.getEndTime()) {
                        return seckillSkuRedis;
                    } else if (seckillSkuRedis != null && seckillSkuRedis.getStartTime() > currentTime) {
                        seckillSkuRedis.setRandomCode(null);
                        return seckillSkuRedis;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        UserLoginTO user = UserLoginInterceptor.loginUser.get();

        // Get Seckill SKU Detail Information
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKU_CACHE);
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        SeckillSkuRedisTO skuRedisTO = JSON.parseObject(json, SeckillSkuRedisTO.class);

        // Verify its legitimacy
        // Verify Time
        Long startTime = skuRedisTO.getStartTime();
        Long endTime = skuRedisTO.getEndTime();
        long currentTime = new Date().getTime();
        if (startTime > currentTime || endTime <= currentTime) {
            return null;
        }
        // Verify Random code
        String skuSessionId = skuRedisTO.getPromotionSessionId() + "_" + skuRedisTO.getSkuId();
        if (!key.equals(skuRedisTO.getRandomCode()) || !killId.equals(skuSessionId)) {
            return null;
        }

        // Verify Quantity
        if (num <= 0 || num > skuRedisTO.getSeckillCount().intValue()) {
            return null;
        }

        // Verify User's Idempotent
        String userSeckillKey = SECKILL_USER_PREFIX + user.getId() + "_" + killId;
        long expire = endTime - currentTime + 60 * 1000;
        Boolean userRedundantOp = redisTemplate.opsForValue().setIfAbsent(userSeckillKey, num.toString(),
                expire, TimeUnit.MILLISECONDS);
        if (Boolean.FALSE.equals(userRedundantOp)) {
            return null;
        }

        // Do Seckill
        RSemaphore lock = redissonClient.getSemaphore(SKU_STOCK_CACHE_PREFIX + key);
        boolean success = lock.tryAcquire(num);
        if (!success) {
            return null;
        }
        // Success and Return OrderSN
        String timeId = IdWorker.getTimeId();

        // Send Message to Rabbit
        SeckillOrderTO seckillOrderTO = new SeckillOrderTO();
        seckillOrderTO.setOrderSn(timeId);
        seckillOrderTO.setMemberId(user.getId());
        seckillOrderTO.setNum(new BigDecimal(num));
        BeanUtils.copyProperties(skuRedisTO, seckillOrderTO);
        rabbitTemplate.convertAndSend(
                "order-event-exchange",
                "order.seckill.order",
                seckillOrderTO
        );

        return timeId;
    }

    /**
     * Save Session Related Sku
     *
     * @param seckillSessions session with skus
     */
    private void saveSessionSku(List<SeckillSessionDTO> seckillSessions) {
        seckillSessions.forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SECKILL_SKU_CACHE);
            session.getRelationSkus().forEach(item -> {
                String skuId = item.getSkuId().toString();
                String sessionSkuKey = session.getId() + "_" + skuId;

                // Idempotent Process
                if (Boolean.TRUE.equals(hashOps.hasKey(sessionSkuKey))) {
                    return;
                }

                SeckillSkuRedisTO skuRedis = new SeckillSkuRedisTO();
                // Save SKU info
                R result = productFeignService.getSkuInfo(item.getSkuId());
                if (result.getCode() == 0) {
                    SkuInfoTO skuInfo = result.getData("skuInfo", new TypeReference<SkuInfoTO>() {
                    });
                    skuRedis.setSkuInfo(skuInfo);
                }

                // Save SKU seckill info
                Long startTime = session.getStartTime().getTime();
                Long endTime = session.getEndTime().getTime();
                skuRedis.setStartTime(startTime);
                skuRedis.setEndTime(endTime);
                BeanUtils.copyProperties(item, skuRedis);

                // Random Code for anti-script attack
                String token = UUID.randomUUID().toString().replace("-", "");
                skuRedis.setRandomCode(token);

                // SET SEMAPHORE for SKU
                // Get Semaphore and Set Semaphore
                RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_CACHE_PREFIX + token);
                semaphore.trySetPermits(item.getSeckillCount().intValue());

                String json = JSON.toJSONString(skuRedis);
                hashOps.put(sessionSkuKey, json);
            });
        });
    }

    /**
     * Save Seckill Session in Redis
     *
     * @param seckillSessions sessions
     */
    private void saveSessionInfo(List<SeckillSessionDTO> seckillSessions) {
        seckillSessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String redisKey = SESSION_CACHE_PREFIX + startTime + "_" + endTime;

            // Idempotent Process
            Boolean hasKey = redisTemplate.hasKey(redisKey);
            if (Boolean.FALSE.equals(hasKey) &&
                    session.getRelationSkus() != null &&
                    session.getRelationSkus().size() > 0) {
                List<String> skuIds = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId())
                        .collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(redisKey, skuIds);
            }
        });
    }

    public SeckillServiceImpl(CouponFeignService couponFeignService,
                              ProductFeignService productFeignService,
                              StringRedisTemplate redisTemplate,
                              RabbitTemplate rabbitTemplate,
                              RedissonClient redissonClient) {
        this.couponFeignService = couponFeignService;
        this.productFeignService = productFeignService;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.redissonClient = redissonClient;
    }
}
