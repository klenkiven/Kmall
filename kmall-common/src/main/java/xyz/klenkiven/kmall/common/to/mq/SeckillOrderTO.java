package xyz.klenkiven.kmall.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Seckill Message to Order
 */
@Data
public class SeckillOrderTO {

    /** Member Id */
    private Long memberId;

    /** Order SN */
    private String orderSn;

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
     * Buy Num
     */
    private BigDecimal num;

}
