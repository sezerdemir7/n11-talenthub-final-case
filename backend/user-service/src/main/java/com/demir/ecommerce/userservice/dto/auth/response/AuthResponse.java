package com.demir.ecommerce.userservice.dto.auth.response;


public record AuthResponse(

        String accessToken,
        String refreshToken

) {
}
