package com.demir.ecommerce.userservice.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.exception.message.TokenErrorMessage;
import com.demir.ecommerce.userservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.key}")
    private String secret;

    @Value("${jwt.expiration-minutes:20}")
    private long expirationMinutes;

    @Override
    public String generateToken(String email) {
        return generateToken(new HashMap<>(), email);
    }

    @Override
    public String generateToken(Long userId, String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put(
                "roles",
                roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .toList()
        );

        return generateToken(claims, email);
    }


    @Override
    public String generateToken(Map<String, Object> claims, String email) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMinutes * 60 * 1000))
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new BusinessException(TokenErrorMessage.TOKEN_GENERATION_FAILED);
        }
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        String tokenEmail = extractEmail(token);
        return tokenEmail.equals(userDetails.getUsername())
                && !extractExpiration(token).before(new Date());
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return validateToken(token, userDetails);
    }

    @Override
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    @Override
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    @Override
    public String extractRole(String token) {
        List<String> roles = extractAllClaims(token).get("roles", List.class);

        if (roles == null || roles.isEmpty()) {
            return null;
        }

        return roles.get(0);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}