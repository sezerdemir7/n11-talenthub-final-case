package com.demir.ecommerce.userservice.dto.seller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSellerProfileRequest(

        @NotBlank
        @Size(max = 150)
        String storeName,

        @NotBlank
        @Size(max = 150)
        String companyName,

        @Size(max = 250)
        String storeDescription
) {
}
