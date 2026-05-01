package com.demir.ecommerce.userservice.dto.address.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(

        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 100)
        String district,

        @NotBlank
        @Size(max = 500)
        String fullAddress,

        @NotBlank
        @Size(max = 20)
        String postalCode,

        Boolean isDefault
) {
}
