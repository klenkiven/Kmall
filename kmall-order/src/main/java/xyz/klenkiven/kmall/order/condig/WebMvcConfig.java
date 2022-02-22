package xyz.klenkiven.kmall.order.condig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/list.html").setViewName("list");
        registry.addViewController("/confirm.html").setViewName("confirm");
        registry.addViewController("/pay.html").setViewName("pay");
        registry.addViewController("/detail.html").setViewName("detail");
    }
}
