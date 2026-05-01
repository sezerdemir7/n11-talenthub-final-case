package com.demir.ecommerce.productservice.dto.product;

import java.math.BigDecimal;

public record ProductListResponse(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        Boolean active,
        Long categoryId,
        String categoryName
) {
}