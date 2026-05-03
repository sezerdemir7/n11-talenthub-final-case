package com.demir.ecommerce.apigateway.unit.security;

import com.demir.ecommerce.apigateway.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "0123456789012345678901234567890123456789012345678901234567890123";
    private static final String EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    @Test
    @DisplayName("Should extract claims from signed token")
    void extractAllClaims_validToken_returnsClaims() {
        String token = Jwts.builder()
                .setSubject(EMAIL)
                .claim("userId", 5L)
                .claim("roles", List.of("ROLE_CUSTOMER"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signKey(), SignatureAlgorithm.HS256)
                .compact();

        Claims claims = jwtService.extractAllClaims(token);

        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.get("userId", Number.class).longValue()).isEqualTo(5L);
        assertThat(claims.get("roles", List.class)).containsExactly("ROLE_CUSTOMER");
    }

    private Key signKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}
