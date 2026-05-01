package com.demir.ecommerce.userservice.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Schema(description = "User first name", example = "Ahmet")
        @NotBlank(message = "First name cannot be blank")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @Schema(description = "User last name", example = "Yilmaz")
        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName

) {}