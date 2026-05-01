package com.demir.ecommerce.orderservice.dto.user;

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
