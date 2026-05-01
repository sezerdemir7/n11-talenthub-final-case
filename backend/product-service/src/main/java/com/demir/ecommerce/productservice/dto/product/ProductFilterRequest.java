package com.demir.ecommerce.productservice.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Schema(description = "Ürün filtreleme isteği")
public record ProductFilterRequest(

        @Schema(description = "Arama anahtar kelimesi", example = "iphone")
        String keyword,

        @Schema(description = "Kategori ID", example = "1")
        Long categoryId,

        @Schema(description = "Satıcı ID", example = "10")
        Long sellerId,

        @Schema(description = "Minimum fiyat", example = "1000")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal minPrice,

        @Schema(description = "Maximum fiyat", example = "50000")
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal maxPrice,

        @Schema(description = "Aktif ürünler", example = "true")
        Boolean active
) {
}