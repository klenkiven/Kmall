package io.renren;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 酷商城-后台管理系统
 * @author klenkiven
 */
@SpringBootApplication
@EnableDiscoveryClient
public class KmallAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(KmallAdminApplication.class, args);
	}

}