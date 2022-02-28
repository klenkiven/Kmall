package xyz.klenkiven.kmall.order.listener;

import com.rabbitmq.client.Channel;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import xyz.klenkiven.kmall.order.entity.OrderEntity;
import xyz.klenkiven.kmall.order.service.OrderService;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCloseListener.class);
    private final OrderService orderService;

    @RabbitHandler
    public void listenRelease(OrderEntity order, Message message, Channel channel) throws IOException {
        try {
            orderService.closeOrder(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
            log.info("Order is delayed: {}, will be close in few time.", order.getOrderSn());
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    public OrderCloseListener(OrderService orderService) {
        this.orderService = orderService;
    }

}
