package com.demir.ecommerce.userservice.dto.seller.request;

import com.demir.ecommerce.userservice.entity.SellerStatus;

public record SellerFilterRequest(
        String storeName,
        SellerStatus status,
        Boolean verified
) {
}