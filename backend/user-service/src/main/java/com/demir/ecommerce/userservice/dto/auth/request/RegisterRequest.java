package com.demir.ecommerce.userservice.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Schema(description = "User first name", example = "Ahmet")
        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @Schema(description = "User last name", example = "Yilmaz")
        @NotBlank(message = "Last name cannot be blank")
        String lastName,

        @Schema(description = "User email address", example = "ahmet@example.com")
        @Email(message = "Email format is invalid")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @Schema(description = "User password", example = "123456")
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password

) {}