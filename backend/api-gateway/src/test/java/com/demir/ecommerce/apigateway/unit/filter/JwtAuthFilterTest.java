package com.demir.ecommerce.apigateway.unit.filter;

import com.demir.ecommerce.apigateway.filter.JwtAuthFilter;
import com.demir.ecommerce.apigateway.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter Unit Tests")
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private GatewayFilterChain chain;

    private JwtAuthFilter filter() {
        return new JwtAuthFilter(jwtService);
    }

    private Claims claims(Long userId, List<String> roles) {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("roles", roles);
        return claims;
    }

    @Nested
    @DisplayName("public paths")
    class PublicPaths {

        @Test
        @DisplayName("Should bypass auth for public auth endpoint")
        void filter_publicAuthPath_callsChain() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/auth/login")
            );
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            filter().filter(exchange, chain).block();

            verify(chain).filter(exchange);
            verify(jwtService, never()).extractAllClaims(org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("Should bypass auth for OPTIONS request")
        void filter_optionsRequest_callsChain() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.method(HttpMethod.OPTIONS, "/api/v1/orders")
            );
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            filter().filter(exchange, chain).block();

            verify(chain).filter(exchange);
            verify(jwtService, never()).extractAllClaims(org.mockito.ArgumentMatchers.anyString());
        }
    }

    @Nested
    @DisplayName("protected paths")
    class ProtectedPaths {

        @Test
        @DisplayName("Should return unauthorized when authorization header is missing")
        void filter_missingAuthorizationHeader_returnsUnauthorized() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/orders")
            );

            filter().filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should return unauthorized when token is invalid")
        void filter_invalidToken_returnsUnauthorized() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/orders")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
            );
            when(jwtService.extractAllClaims("invalid-token")).thenThrow(new JwtException("invalid"));

            filter().filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should return unauthorized when token has no user id")
        void filter_missingUserIdClaim_returnsUnauthorized() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/orders")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
            );
            when(jwtService.extractAllClaims("token")).thenReturn(claims(null, List.of("ROLE_CUSTOMER")));

            filter().filter(exchange, chain).block();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(chain, never()).filter(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("Should add user headers for valid token")
        void filter_validToken_addsUserHeadersAndCallsChain() {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/v1/orders")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                            .header("X-User-Id", "old")
                            .header("X-User-Roles", "old")
            );
            when(jwtService.extractAllClaims("token")).thenReturn(claims(7L, List.of("ROLE_CUSTOMER", "ADMIN")));

            AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
            when(chain.filter(org.mockito.ArgumentMatchers.any(ServerWebExchange.class))).thenAnswer(invocation -> {
                capturedExchange.set(invocation.getArgument(0));
                return Mono.empty();
            });

            filter().filter(exchange, chain).block();

            ServerWebExchange mutatedExchange = capturedExchange.get();
            assertThat(mutatedExchange).isNotNull();
            assertThat(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("7");
            assertThat(mutatedExchange.getRequest().getHeaders().getFirst("X-User-Roles")).isEqualTo("CUSTOMER,ADMIN");
        }
    }

    @Test
    @DisplayName("Should run with highest precedence")
    void getOrder_returnsHighestPrecedence() {
        assertThat(filter().getOrder()).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
    }
}
