package com.demir.ecommerce.apigateway.integration;

import com.demir.ecommerce.apigateway.filter.JwtAuthFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtAuthFilter Integration Tests")
class JwtAuthFilterIntegrationTest {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    private static final String SECRET = "0123456789012345678901234567890123456789012345678901234567890123";

    @Nested
    @DisplayName("protected requests")
    class ProtectedRequests {

        @Test
        @DisplayName("Should reject protected request without token")
        void filter_missingToken_returnsUnauthorized() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/orders")
            );

            jwtAuthFilter.filter(exchange, ignored -> Mono.empty()).block();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should parse real JWT and forward user headers")
        void filter_validJwt_addsUserHeaders() {
            String token = token();
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/orders")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            );
            AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
            GatewayFilterChain chain = mutatedExchange -> {
                capturedExchange.set(mutatedExchange);
                return Mono.empty();
            };

            jwtAuthFilter.filter(exchange, chain).block();

            assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("42");
            assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Roles")).isEqualTo("CUSTOMER,SELLER");
        }
    }

    @Nested
    @DisplayName("public requests")
    class PublicRequests {

        @Test
        @DisplayName("Should allow public auth request without token")
        void filter_publicAuthPath_callsChain() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/auth/login")
            );
            AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();

            jwtAuthFilter.filter(exchange, mutatedExchange -> {
                capturedExchange.set(mutatedExchange);
                return Mono.empty();
            }).block();

            assertThat(capturedExchange.get()).isSameAs(exchange);
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }
    }

    private String token() {
        return Jwts.builder()
                .setSubject("seller@test.com")
                .claim("userId", 42L)
                .claim("roles", List.of("ROLE_CUSTOMER", "ROLE_SELLER"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key signKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}
