package com.demir.ecommerce.apigateway.filter;

import com.demir.ecommerce.apigateway.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> publicPaths = List.of(
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info"
    );

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());

            Claims claims = jwtService.extractAllClaims(token);

            Long userId = claims.get("userId", Long.class);
            List<String> roles = claims.get("roles", List.class);

            if (userId == null) {
                return unauthorized(exchange);
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.remove(USER_ROLES_HEADER);

                        headers.add(USER_ID_HEADER, String.valueOf(userId));

                        if (roles != null && !roles.isEmpty()) {
                            headers.add(USER_ROLES_HEADER, String.join(",", roles));
                        }
                    })
                    .build();

            return chain.filter(
                    exchange.mutate()
                            .request(mutatedRequest)
                            .build()
            );

        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

/*
package com.demir.ecommerce.apigateway.filter;

import com.demir.ecommerce.apigateway.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_SELLER = "SELLER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> publicPaths = List.of(
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/actuator/info"
    );

    private final List<RoleRule> roleRules = List.of(
            new RoleRule("/api/v1/admin/**", List.of(ROLE_ADMIN)),

            new RoleRule("/api/v1/cart/**", List.of(ROLE_CUSTOMER, ROLE_ADMIN)),
            new RoleRule("/api/v1/orders/**", List.of(ROLE_CUSTOMER, ROLE_ADMIN)),

            new RoleRule("/api/v1/seller/**", List.of(ROLE_SELLER, ROLE_ADMIN)),
            new RoleRule("/api/v1/products/manage/**", List.of(ROLE_SELLER, ROLE_ADMIN))
    );

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());
            Claims claims = jwtService.extractAllClaims(token);

            Long userId = extractUserId(claims);
            List<String> roles = extractRoles(claims);

            if (userId == null) {
                return unauthorized(exchange);
            }

            if (isForbiddenByRole(path, roles)) {
                return forbidden(exchange);
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {
                        headers.remove(USER_ID_HEADER);
                        headers.remove(USER_ROLES_HEADER);

                        headers.add(USER_ID_HEADER, String.valueOf(userId));

                        if (!roles.isEmpty()) {
                            headers.add(USER_ROLES_HEADER, String.join(",", roles));
                        }
                    })
                    .build();

            return chain.filter(
                    exchange.mutate()
                            .request(mutatedRequest)
                            .build()
            );

        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(exchange);
        }
    }

    private Long extractUserId(Claims claims) {
        Number userId = claims.get("userId", Number.class);
        return userId == null ? null : userId.longValue();
    }

    private List<String> extractRoles(Claims claims) {
        Object rolesClaim = claims.get("roles");

        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(String::valueOf)
                .toList();
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean isForbiddenByRole(String path, List<String> userRoles) {
        return roleRules.stream()
                .filter(rule -> pathMatcher.match(rule.pattern(), path))
                .anyMatch(rule -> rule.requiredRoles().stream().noneMatch(userRoles::contains));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private record RoleRule(String pattern, List<String> requiredRoles) {
    }
}

 */
