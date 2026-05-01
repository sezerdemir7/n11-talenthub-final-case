package com.demir.ecommerce.userservice.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerRegisterRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @NotBlank
        @Size(max = 150)
        String storeName,

        @NotBlank
        @Size(max = 150)
        String companyName,

        @NotBlank
        @Size(max = 50)
        String taxNumber,

        @Size(max = 250)
        String storeDescription
) {
}
