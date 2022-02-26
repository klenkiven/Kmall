package xyz.klenkiven.kmall.ware.config;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ Config
 * @author klenkiven
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    /**
     * Use Json Message Converter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /* Business Related Component  */
    /** stockDelayQueue */
    @Bean public Queue stockDelayQueue() {
        Map<String, Object> argument = new HashMap<>();
        argument.put("x-dead-letter-exchange", "stock-event-exchange");
        argument.put("x-dead-letter-routing-key", "stock.release.stock.queue");
        argument.put("x-message-ttl", 2 * 60 * 1000);
        System.out.println(argument);
        return new Queue(
                "stock.delay.queue",
                true,
                false,
                false,
                argument
        );
    }

    /** stockReleaseStockQueue */
    @Bean public Queue stockReleaseStockQueue() {
        return new Queue(
                "stock.release.stock.queue",
                true,
                false,
                false,
                null
        );
    }

    /** stock-event-exchange */
    @Bean public Exchange stockEventExchange() {
        return new TopicExchange(
                "stock-event-exchange",
                true,
                false,
                null
        );
    }

    /** Stock Release Binding */
    @Bean public Binding stockReleaseBinding() {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null
        );
    }

    /** Stock Locked Binding */
    @Bean public Binding stockLockedBinding() {
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null
        );
    }

}
