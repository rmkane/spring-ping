package org.acme.ping.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.acme.ping.filter.HeadersDebugResponseFinalizeFilter;
import org.acme.security.properties.AppSecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class HeadersDebugFilterConfig {

    @Bean
    FilterRegistrationBean<HeadersDebugResponseFinalizeFilter> headersDebugResponseFinalizeFilter(
            ObjectMapper objectMapper, AppSecurityProperties appSecurityProperties) {
        var bean = new FilterRegistrationBean<>(
                new HeadersDebugResponseFinalizeFilter(objectMapper, appSecurityProperties));
        bean.setOrder(Ordered.LOWEST_PRECEDENCE);
        bean.addUrlPatterns("/api/debug/headers");
        return bean;
    }
}
