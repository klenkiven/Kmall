package xyz.klenkiven.kmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@EnableRabbit
@Configuration
public class RabbitConfig {

    /**
     * Use Json Message Converter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /* Business Related Component  */
    /**
     * Order Seckill Queue for Absorb Huge Traffic at a time
     */
    @Bean public Queue orderSeckillOrderQueue() {
        return new Queue(
                "order.seckill.order.queue",
                true,
                false,
                false,
                null
        );
    }

    /**
     * Biding for Order Event Exchange to Order Seckill Queue
     */
    @Bean public Binding orderSeckillOrder() {
        return new Binding(
                "order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null
        );
    }

    /** orderDelayQueue */
    @Bean public Queue orderDelayQueue() {
        Map<String, Object> argument = new HashMap<>();
        argument.put("x-dead-letter-exchange", "order-event-exchange");
        argument.put("x-dead-letter-routing-key", "order.release.order");
        argument.put("x-message-ttl", 60 * 1000);
        return new Queue(
                "order.delay.queue",
                true,
                false,
                false,
                argument
        );
    }

    /** orderReleaseOrderQueue */
    @Bean public Queue orderReleaseOrderQueue() {
        return new Queue(
                "order.release.order.queue",
                true,
                false,
                false,
                null
        );
    }

    /** orderEventExchange */
    @Bean public Exchange orderEventExchange() {
        return new TopicExchange(
                "order-event-exchange",
                true,
                false,
                null
        );
    }

    /** orderCreateOrderBinding */
    @Bean public Binding orderCreateOrderBinding() {
        return new Binding(
                "order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null
        );
    }

    /** orderReleaseOrderBinding */
    @Bean public Binding orderReleaseOrderBinding() {
        return new Binding(
                "order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null
        );
    }

    /**
     * Release Order and Notify Ware service to Unlock Stock
     */
    @Bean public Binding orderReleaseOtherBinding() {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null
        );
    }
}
