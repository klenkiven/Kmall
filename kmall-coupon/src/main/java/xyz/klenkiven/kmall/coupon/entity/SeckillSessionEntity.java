package xyz.klenkiven.kmall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 秒杀活动场次
 * 
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-08-29 20:46:13
 */
@Data
@TableName("sms_seckill_session")
public class SeckillSessionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
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
	@TableField(exist = false)
	private List<SeckillSkuRelationEntity> relationSkus;
}
