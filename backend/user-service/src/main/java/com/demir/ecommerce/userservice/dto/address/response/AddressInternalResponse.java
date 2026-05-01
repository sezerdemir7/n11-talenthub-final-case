package com.demir.ecommerce.userservice.dto.address.response;

public record AddressInternalResponse(
        Long id,
        Long userId,
        String title,
        String city,
        String district,
        String fullAddress,
        String postalCode,
        Boolean isDefault
) {
}
