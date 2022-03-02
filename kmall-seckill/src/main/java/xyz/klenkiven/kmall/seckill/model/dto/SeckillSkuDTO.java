package xyz.klenkiven.kmall.seckill.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * [FEIGN] Seckill Sku Related DTO from kmall-coupon
 * @author klenkiven
 */
@Data
public class SeckillSkuDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
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

}
