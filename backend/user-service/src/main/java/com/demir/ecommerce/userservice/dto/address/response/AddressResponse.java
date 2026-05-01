package com.demir.ecommerce.userservice.dto.address.response;

public record AddressResponse(
        Long id,
        String title,
        String city,
        String district,
        String fullAddress,
        String postalCode,
        Boolean isDefault
) {
}
