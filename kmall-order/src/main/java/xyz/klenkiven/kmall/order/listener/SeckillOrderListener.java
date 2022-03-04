package xyz.klenkiven.kmall.order.listener;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import xyz.klenkiven.kmall.common.to.mq.SeckillOrderTO;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.service.OrderService;

import java.io.IOException;

/**
 * Order Seckill Listener for Seckill Service
 * <p>When user 'kill' one item, and seckill will send order SN,
 * to 'order.seckill.order.queue' and order service need to handle
 * those message.</p>
 * @author klenkiven
 */
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class SeckillOrderListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCloseListener.class);
    private final OrderService orderService;

    /**
     * Handle Seckill Order
     */
    @RabbitHandler
    public void seckillOrderHandle(SeckillOrderTO seckill, Message message, Channel channel) throws IOException {
        try {
            long startTime = System.currentTimeMillis();
            orderService.createSeckillOrder(seckill);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            log.info("Seckill Order is created: {}, cost time: " +
                    (System.currentTimeMillis() - startTime) + "ms", seckill.getOrderSn());
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    public SeckillOrderListener(OrderService orderService) {
        this.orderService = orderService;
    }

}
