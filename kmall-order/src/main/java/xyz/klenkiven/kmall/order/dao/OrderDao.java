package xyz.klenkiven.kmall.order.dao;

import org.apache.ibatis.annotations.Param;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-08-29 21:06:10
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    /**
     * Update Trade Status
     * @param out_trade_no order SN
     * @param code status
     * @return
     */
    int updateOrderStatus(@Param("orderSn") String out_trade_no, @Param("status") Integer code);
}
