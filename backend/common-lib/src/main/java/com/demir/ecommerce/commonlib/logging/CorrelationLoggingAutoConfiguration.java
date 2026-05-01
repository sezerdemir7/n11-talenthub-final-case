package com.demir.ecommerce.commonlib.logging;

import feign.RequestInterceptor;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class CorrelationLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CorrelationIdFilter.class)
    public Filter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    @ConditionalOnMissingBean(AccessLogFilter.class)
    public Filter accessLogFilter() {
        return new AccessLogFilter();
    }

    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    @ConditionalOnMissingBean(name = "correlationFeignInterceptor")
    public RequestInterceptor correlationFeignInterceptor() {
        return new CorrelationFeignInterceptor();
    }
}
