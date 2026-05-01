package com.demir.ecommerce.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Configuration
public class AccessLogGatewayFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogGatewayFilter.class);

    @Bean
    public GlobalFilter accessLogGlobalFilter() {
        return (exchange, chain) -> {
            long startedAt = System.currentTimeMillis();

            return chain.filter(exchange)
                    .doFinally(signalType -> {
                        long durationMs = System.currentTimeMillis() - startedAt;

                        ServerHttpRequest request = exchange.getRequest();

                        String correlationId = request.getHeaders().getFirst("X-Correlation-Id");
                        String clientIp = getClientIp(request);

                        MDC.put("correlationId", correlationId);
                        MDC.put("http.method", request.getMethod().name());
                        MDC.put("http.path", request.getURI().getPath());
                        MDC.put("http.status", getStatus(exchange));
                        MDC.put("http.duration_ms", String.valueOf(durationMs));
                        MDC.put("client.ip", clientIp);

                        try {
                            log.info("HTTP request completed");
                        } finally {
                            MDC.remove("correlationId");
                            MDC.remove("http.method");
                            MDC.remove("http.path");
                            MDC.remove("http.status");
                            MDC.remove("http.duration_ms");
                            MDC.remove("client.ip");
                        }
                    });
        };
    }

    @Bean
    public Ordered accessLogGlobalFilterOrder() {
        return () -> -90;
    }

    private String getClientIp(ServerHttpRequest request) {
        String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        if (request.getRemoteAddress() == null) {
            return "unknown";
        }

        return request.getRemoteAddress().getAddress().getHostAddress();
    }

    private String getStatus(org.springframework.web.server.ServerWebExchange exchange) {
        if (exchange.getResponse().getStatusCode() == null) {
            return "unknown";
        }

        return String.valueOf(exchange.getResponse().getStatusCode().value());
    }
}
