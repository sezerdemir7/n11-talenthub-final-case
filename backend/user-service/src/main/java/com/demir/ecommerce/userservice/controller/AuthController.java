package com.demir.ecommerce.userservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.userservice.dto.auth.request.LoginRequest;
import com.demir.ecommerce.userservice.dto.auth.request.RegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.request.SellerRegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.response.AuthResponse;
import com.demir.ecommerce.userservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Authentication operations")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "User registration", description = "Registers a new customer and returns tokens")
    @PostMapping("/register")
    public ResponseEntity<RestResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(RestResponse.of(response, "User registered successfully"));
    }

    @Operation(summary = "Seller registration", description = "Registers a new seller application and returns tokens")
    @PostMapping("/register/seller")
    public ResponseEntity<RestResponse<AuthResponse>> registerSeller(
            @Valid @RequestBody SellerRegisterRequest request
    ) {
        AuthResponse response = authService.registerSeller(request);
        return ResponseEntity.ok(RestResponse.of(response, "Seller registration created successfully"));
    }

    @Operation(summary = "User login", description = "Authenticates user and returns tokens")
    @PostMapping("/login")
    public ResponseEntity<RestResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(RestResponse.of(response, "Login successful"));
    }

    @Operation(summary = "Refresh token", description = "Generates new access and refresh tokens")
    @PostMapping("/refresh-token")
    public ResponseEntity<RestResponse<AuthResponse>> refreshToken(
            @RequestParam String refreshToken
    ) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(RestResponse.of(response, "Token refreshed"));
    }

    @Operation(summary = "Logout", description = "Revokes refresh token")
    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logout(
            @RequestParam String refreshToken
    ) {
        authService.logout(refreshToken);
        return ResponseEntity.ok(RestResponse.success("Logout successful"));
    }
}
