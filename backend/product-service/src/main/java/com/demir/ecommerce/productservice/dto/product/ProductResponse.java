package com.demir.ecommerce.productservice.dto.product;

import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long sellerId,
        String name,
        String slug,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        Boolean active,
        Long categoryId,
        String categoryName,
        ProductDetailResponse detail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}