package com.demir.ecommerce.productservice.dto.category.response;

public record CategoryFilterResponse(
        Long id,
        String name,
        String slug,
        Long parentId,
        Long productCount
) {
}