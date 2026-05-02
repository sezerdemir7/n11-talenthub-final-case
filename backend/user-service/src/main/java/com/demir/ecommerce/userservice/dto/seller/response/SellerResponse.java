package com.demir.ecommerce.userservice.dto.seller.response;

import com.demir.ecommerce.userservice.entity.SellerStatus;

public record SellerResponse(
        Long userId,
        String storeName,
        String companyName,
        SellerStatus status,
        Boolean verified
) {
}