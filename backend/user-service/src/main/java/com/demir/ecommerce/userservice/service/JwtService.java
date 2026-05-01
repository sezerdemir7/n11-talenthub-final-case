package com.demir.ecommerce.userservice.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface JwtService {

    String generateToken(String email);

    String generateToken(Long userId, String email, List<String> roles);

    String generateToken(Map<String, Object> claims, String email);

    boolean validateToken(String token, UserDetails userDetails);

    boolean isTokenValid(String token, UserDetails userDetails);

    String extractEmail(String token);

    Long extractUserId(String token);

    String extractRole(String token);
}