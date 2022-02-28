package xyz.klenkiven.kmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.model.form.OrderSubmitForm;
import xyz.klenkiven.kmall.order.model.vo.OrderConfirmVO;
import xyz.klenkiven.kmall.order.model.vo.SubmitResultVO;

import java.util.Map;

/**
 * 订单
 *
 * @author klenkiven
 * @email wzl709@outlook.com
 * @date 2021-08-29 21:06:10
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * Order Confirm Page needed data
     * @return data
     */
    OrderConfirmVO confirmOrder();

    /**
     * Do Order Submit
     * @param form submit form
     * @return result
     */
    SubmitResultVO submitOrder(OrderSubmitForm form);

    /**
     * Get Order By OrderSN
     * @param orderSn order SN
     * @return order
     */
    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * When order is timeout, need to close order
     * @param order order entity
     */
    void closeOrder(OrderEntity order);
}

