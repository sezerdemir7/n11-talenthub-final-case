package com.demir.ecommerce.productservice.dto.productdetail;

public record ProductDetailResponse(
        Long id,
        String shortDescription,
        String longDescription,
        String brand,
        String model,
        String warrantyPeriod,
        String specifications
) {
}