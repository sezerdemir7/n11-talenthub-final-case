package com.demir.ecommerce.productservice.dto.product;

import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailUpdateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Ürün güncelleme isteği")
public record ProductUpdateRequest(

        @Schema(description = "Ürün adı", example = "iPhone 15 Pro Max")
        @NotBlank
        String name,

        @Schema(description = "Fiyat", example = "59999.99")
        @NotNull
        @Positive
        BigDecimal price,

        @Schema(description = "Stok", example = "50")
        @NotNull
        @Min(0)
        Integer stock,

        @Schema(description = "Aktif mi?", example = "true")
        @NotNull
        Boolean active,

        @Schema(description = "Kategori ID", example = "2")
        @NotNull
        Long categoryId,

        @Schema(description = "Detay bilgisi")
        @Valid
        @NotNull
        ProductDetailUpdateRequest detail
) {
}