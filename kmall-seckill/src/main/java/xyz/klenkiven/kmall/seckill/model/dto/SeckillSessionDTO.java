package xyz.klenkiven.kmall.seckill.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * [FEIGN] Seckill Service Session DTO from kmall-coupon
 * @author klenkiven
 */
@Data
public class SeckillSessionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * Related SKU
     */
    private List<SeckillSkuDTO> relationSkus;
}
