package com.demir.ecommerce.userservice.unit.service.impl;

import com.demir.ecommerce.userservice.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtServiceImpl Unit Tests")
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    private static final String SECRET = "0123456789012345678901234567890123456789012345678901234567890123";
    private static final String EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMinutes", 20L);
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Should generate token with user id, email and roles")
        void generateToken_withUserClaims_createsReadableToken() {
            String token = jwtService.generateToken(1L, EMAIL, List.of("CUSTOMER"));

            assertThat(jwtService.extractEmail(token)).isEqualTo(EMAIL);
            assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
            assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("Should keep role prefix when role already starts with ROLE")
        void generateToken_roleAlreadyPrefixed_doesNotDuplicatePrefix() {
            String token = jwtService.generateToken(1L, EMAIL, List.of("ROLE_ADMIN"));

            assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_ADMIN");
        }
    }

    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("Should validate token for matching user")
        void validateToken_matchingUser_returnsTrue() {
            String token = jwtService.generateToken(1L, EMAIL, List.of("CUSTOMER"));
            UserDetails userDetails = User.withUsername(EMAIL)
                    .password("password")
                    .authorities("ROLE_CUSTOMER")
                    .build();

            assertThat(jwtService.validateToken(token, userDetails)).isTrue();
        }

        @Test
        @DisplayName("Should reject token for different user")
        void validateToken_differentUser_returnsFalse() {
            String token = jwtService.generateToken(1L, EMAIL, List.of("CUSTOMER"));
            UserDetails userDetails = User.withUsername("other@test.com")
                    .password("password")
                    .authorities("ROLE_CUSTOMER")
                    .build();

            assertThat(jwtService.validateToken(token, userDetails)).isFalse();
        }
    }
}
