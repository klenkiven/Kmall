package xyz.klenkiven.kmall.seckill.service;

import xyz.klenkiven.kmall.common.to.SeckillSkuRedisTO;

import java.util.List;

/**
 * Seckill Service
 * @author klenkiven
 */
public interface SeckillService {

    /**
     * Upload Seckill SKU Latest 3 days
     */
    void uploadSeckillSkuLatest3Day();

    /**
     * Get all of seckill sku
     * @return skus
     */
    List<SeckillSkuRedisTO> getCurrentSeckillSkus();

    /**
     * Get SKU Seckill Info
     * @param skuID sku id
     * @return seckill sku info
     */
    SeckillSkuRedisTO getSkuSeckill(Long skuID);

    /**
     * SecKill Core Serve Method
     * @param killId kill id
     * @param key random key
     * @param num buy num
     * @return order SN
     */
    String kill(String killId, String key, Integer num);
}
