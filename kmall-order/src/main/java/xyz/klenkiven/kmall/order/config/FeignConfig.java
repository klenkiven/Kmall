package xyz.klenkiven.kmall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Configuration for RequestInterceptor
 */
@Configuration
public class FeignConfig {

    /**
     * Interceptor for Feign to Add Origin Request's Cookie Info
     */
    @Bean
    public RequestInterceptor cookieInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String cookie = requestAttributes.getRequest().getHeader("Cookie");
            template.header("Cookie", cookie);
        };
    }

}
