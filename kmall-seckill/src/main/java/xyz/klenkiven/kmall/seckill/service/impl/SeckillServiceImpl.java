package xyz.klenkiven.kmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.klenkiven.kmall.common.exception.NoStockException;
import xyz.klenkiven.kmall.common.utils.R;
import xyz.klenkiven.kmall.seckill.feign.CouponFeignService;
import xyz.klenkiven.kmall.seckill.feign.ProductFeignService;
import xyz.klenkiven.kmall.seckill.model.dto.SeckillSessionDTO;
import xyz.klenkiven.kmall.seckill.model.dto.SkuInfoDTO;
import xyz.klenkiven.kmall.seckill.model.to.SeckillSkuRedisTO;
import xyz.klenkiven.kmall.seckill.service.SeckillService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    private static final String SECKILL_CACHE_PREFIX = "seckill:skus";
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

    /**
     * Save Session Related Sku
     *
     * @param seckillSessions session with skus
     */
    private void saveSessionSku(List<SeckillSessionDTO> seckillSessions) {
        seckillSessions.forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SECKILL_CACHE_PREFIX);
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
                    SkuInfoDTO skuInfo = result.getData("skuInfo", new TypeReference<SkuInfoDTO>() {
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
            long endTime = session.getStartTime().getTime();
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
