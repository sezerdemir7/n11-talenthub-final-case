package com.demir.ecommerce.userservice.service;

import com.demir.ecommerce.userservice.dto.auth.request.LoginRequest;
import com.demir.ecommerce.userservice.dto.auth.request.RegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.request.SellerRegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse registerSeller(SellerRegisterRequest request);


    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);
}
