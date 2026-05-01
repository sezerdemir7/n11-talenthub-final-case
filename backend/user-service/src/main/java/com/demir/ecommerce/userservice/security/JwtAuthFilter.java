package com.demir.ecommerce.userservice.security;

import com.demir.ecommerce.userservice.exception.message.TokenErrorMessage;
import com.demir.ecommerce.userservice.exception.message.UserErrorMessage;
import com.demir.ecommerce.userservice.service.JwtService;
import com.demir.ecommerce.userservice.util.SecurityResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService,
                         CustomUserDetailsService userDetailsService,
                         ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        final String email;

        try {
            email = jwtService.extractEmail(token);
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            SecurityResponseUtil.writeErrorResponse(
                    response,
                    objectMapper,
                    TokenErrorMessage.TOKEN_EXPIRED,
                    request.getRequestURI()
            );
            return;
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            SecurityResponseUtil.writeErrorResponse(
                    response,
                    objectMapper,
                    TokenErrorMessage.INVALID_TOKEN,
                    request.getRequestURI()
            );
            return;
        }

        if (email != null
                && !email.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (!jwtService.isTokenValid(token, userDetails)) {
                    SecurityContextHolder.clearContext();
                    SecurityResponseUtil.writeErrorResponse(
                            response,
                            objectMapper,
                            TokenErrorMessage.INVALID_TOKEN,
                            request.getRequestURI()
                    );
                    return;
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (UsernameNotFoundException e) {
                SecurityContextHolder.clearContext();
                SecurityResponseUtil.writeErrorResponse(
                        response,
                        objectMapper,
                        UserErrorMessage.USER_NOT_FOUND,
                        request.getRequestURI()
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}