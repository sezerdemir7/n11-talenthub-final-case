package com.demir.ecommerce.userservice.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @Schema(description = "User email", example = "user@example.com")
        @Email(message = "Email format is invalid")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @Schema(description = "User password", example = "123456")
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password

) {}