package xyz.klenkiven.kmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.klenkiven.kmall.common.to.SkuInfoTO;
import xyz.klenkiven.kmall.common.utils.R;
import xyz.klenkiven.kmall.seckill.feign.CouponFeignService;
import xyz.klenkiven.kmall.seckill.feign.ProductFeignService;
import xyz.klenkiven.kmall.seckill.model.dto.SeckillSessionDTO;
import xyz.klenkiven.kmall.common.to.SeckillSkuRedisTO;
import xyz.klenkiven.kmall.seckill.service.SeckillService;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private static final String SECKILL_SKU_CACHE_PREFIX = "seckill:skus";
    private static final String SKU_STOCK_CACHE_PREFIX = "seckill:stock:";

    private final CouponFeignService couponFeignService;
    private final ProductFeignService productFeignService;
    private final StringRedisTemplate redisTemplate;

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
                        redisTemplate.boundHashOps(SECKILL_SKU_CACHE_PREFIX);
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
        BoundHashOperations<String,String, String> hashOps = redisTemplate.boundHashOps(SECKILL_SKU_CACHE_PREFIX);
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
                    } else if (seckillSkuRedis != null && seckillSkuRedis.getStartTime() < currentTime) {
                        seckillSkuRedis.setRandomCode(null);
                        return seckillSkuRedis;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Save Session Related Sku
     *
     * @param seckillSessions session with skus
     */
    private void saveSessionSku(List<SeckillSessionDTO> seckillSessions) {
        seckillSessions.forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SECKILL_SKU_CACHE_PREFIX);
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
                              RedissonClient redissonClient) {
        this.couponFeignService = couponFeignService;
        this.productFeignService = productFeignService;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }
}
