package com.demir.ecommerce.productservice.dto.category.response;

import java.util.List;

public record CategoryTreeResponse(
        Long id,
        String name,
        String slug,
        Integer sortOrder,
        List<CategoryTreeResponse> children
) {
}