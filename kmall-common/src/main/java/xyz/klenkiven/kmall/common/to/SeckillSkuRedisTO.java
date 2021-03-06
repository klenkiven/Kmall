package xyz.klenkiven.kmall.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Seckill Save To Redis
 * @author klenkiven
 */
@Data
public class SeckillSkuRedisTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 活动id
     */
    private Long promotionId;

    /**
     * seckill Random Code
     */
    private String randomCode;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    /**
     * SKU info
     */
    private SkuInfoTO skuInfo;

    /**
     * Session Start Time
     */
    private Long startTime;

    /**
     * Session End Time
     */
    private Long endTime;

}
