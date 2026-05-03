package com.demir.ecommerce.userservice.integration.service.impl;

import com.demir.ecommerce.userservice.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtServiceImpl Integration Tests")
class JwtServiceImplIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private static final String EMAIL = "user@test.com";

    @Nested
    @DisplayName("token claims")
    class TokenClaims {

        @Test
        @DisplayName("Should generate token and extract claims with configured test secret")
        void generateToken_extractClaims_returnsExpectedValues() {
            String token = jwtService.generateToken(1L, EMAIL, List.of("CUSTOMER", "SELLER"));

            assertThat(jwtService.extractEmail(token)).isEqualTo(EMAIL);
            assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
            assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_CUSTOMER");
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("Should validate token for matching user details")
        void validateToken_matchingUser_returnsTrue() {
            String token = jwtService.generateToken(1L, EMAIL, List.of("CUSTOMER"));
            UserDetails userDetails = User.withUsername(EMAIL)
                    .password("password")
                    .authorities("ROLE_CUSTOMER")
                    .build();

            assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
        }
    }
}
