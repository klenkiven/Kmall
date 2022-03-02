package xyz.klenkiven.kmall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Kmall Second Kill Service
 * @author klenkiven
 */
@EnableFeignClients
@EnableDiscoveryClient
@EnableRedisHttpSession
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class KmallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(KmallSeckillApplication.class, args);
    }

}
