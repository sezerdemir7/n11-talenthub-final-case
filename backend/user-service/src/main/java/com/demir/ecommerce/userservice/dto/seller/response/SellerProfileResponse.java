package com.demir.ecommerce.userservice.dto.seller.response;

import com.demir.ecommerce.userservice.entity.SellerStatus;

public record SellerProfileResponse(
        Long id,
        Long userId,
        String storeName,
        String companyName,
        String taxNumber,
        String storeDescription,
        SellerStatus status,
        Boolean verified
) {
}
