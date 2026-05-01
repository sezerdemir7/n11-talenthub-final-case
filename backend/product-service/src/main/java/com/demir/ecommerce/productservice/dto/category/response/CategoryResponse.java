package com.demir.ecommerce.productservice.dto.category.response;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Long parentId,
        String parentName,
        Boolean active,
        Integer sortOrder,
        List<CategoryResponse> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}