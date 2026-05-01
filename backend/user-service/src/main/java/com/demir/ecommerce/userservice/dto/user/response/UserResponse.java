package com.demir.ecommerce.userservice.dto.user.response;


import java.util.List;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        List<String> roles,
        boolean active
) {
}