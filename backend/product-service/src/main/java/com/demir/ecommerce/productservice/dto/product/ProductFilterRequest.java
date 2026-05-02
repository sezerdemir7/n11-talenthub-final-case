package com.demir.ecommerce.productservice.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Schema(description = "Product filtering request")
public record ProductFilterRequest(

        @Schema(description = "Search keyword", example = "iphone")
        String keyword,

        @Schema(description = "Category ID — includes subcategories", example = "1")
        Long categoryId,

        @Schema(description = "Seller ID", example = "10")
        Long sellerId,

        @Schema(description = "Minimum price", example = "1000")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal minPrice,

        @Schema(description = "Maximum price", example = "50000")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal maxPrice,

        @Schema(description = "Active products only", example = "true")
        Boolean active,

        @Schema(description = "Brand filter", example = "Apple")
        String brand,

        @Schema(description = "Sorting option", example = "NEWEST")
        SortOption sortBy
) {
}