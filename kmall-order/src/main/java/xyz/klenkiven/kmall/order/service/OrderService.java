package xyz.klenkiven.kmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.klenkiven.kmall.common.to.mq.SeckillOrderTO;
import xyz.klenkiven.kmall.common.utils.PageUtils;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.model.form.OrderSubmitForm;
import xyz.klenkiven.kmall.order.model.vo.OrderConfirmVO;
import xyz.klenkiven.kmall.order.model.vo.PayReturnVO;
import xyz.klenkiven.kmall.order.model.vo.PayVO;
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

    /**
     * Get Current Order's Payment Information
     * @param orderSn order sequence number
     * @return payment info
     */
    PayVO getOrderPay(String orderSn);

    /**
     * Query Order and Its Items
     * @param params params
     * @return OrderDetail
     */
    PageUtils queryPageWithItem(Map<String, Object> params);

    /**
     * Handle Alipay Return Callback
     * @param payReturn alipay callback argument
     * @return result
     */
    String handlePayResult(PayReturnVO payReturn);

    /**
     * Handle Seckill Created Order
     */
    void createSeckillOrder(SeckillOrderTO seckill);
}

