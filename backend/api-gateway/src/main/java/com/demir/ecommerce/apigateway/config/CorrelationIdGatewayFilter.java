package com.demir.ecommerce.apigateway.config;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.UUID;

@Configuration
public class CorrelationIdGatewayFilter {

    private static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Bean
    public GlobalFilter correlationGlobalFilter() {
        return (exchange, chain) -> {
            String correlationId = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HEADER);

            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header(HEADER, correlationId)
                    .build();

            String finalCorrelationId = correlationId;

            return chain.filter(exchange.mutate().request(request).build())
                    .doFirst(() -> MDC.put(MDC_KEY, finalCorrelationId))
                    .doFinally(signalType -> MDC.remove(MDC_KEY));
        };
    }

    @Bean
    public Ordered correlationGlobalFilterOrder() {
        return () -> -100;
    }
}
