package xyz.klenkiven.kmall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 酷商城-订单管理
 * @author klenkiven
 */
@EnableRedisHttpSession
@EnableRabbit
@SpringBootApplication
@EnableDiscoveryClient
public class KmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmallOrderApplication.class, args);
    }

}
