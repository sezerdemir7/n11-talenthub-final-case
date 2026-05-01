package com.demir.ecommerce.productservice.dto.product;

import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailCreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Yeni ürün oluşturma isteği")
public record ProductCreateRequest(

        @Schema(description = "Ürün adı", example = "iPhone 15 Pro")
        @NotBlank(message = "Ürün adı boş olamaz")
        String name,

        @Schema(description = "Ürün fiyatı", example = "49999.99")
        @NotNull
        @Positive
        BigDecimal price,

        @Schema(description = "Stok miktarı", example = "100")
        @NotNull
        @Min(0)
        Integer stock,

        @Schema(description = "Ürün aktif mi?", example = "true")
        @NotNull
        Boolean active,

        @Schema(description = "Kategori ID", example = "1")
        @NotNull
        Long categoryId,

        @Schema(description = "Ürün detay bilgisi")
        @Valid
        @NotNull
        ProductDetailCreateRequest detail
) {
}