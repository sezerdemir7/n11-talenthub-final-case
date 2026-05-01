package com.demir.ecommerce.productservice.dto.productdetail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Ürün detay oluşturma isteği")
public record ProductDetailCreateRequest(

        @Schema(description = "Kısa açıklama", example = "Yeni nesil iPhone")
        @NotBlank
        String shortDescription,

        @Schema(description = "Uzun açıklama", example = "Apple A17 Pro işlemcili...")
        @NotBlank
        String longDescription,

        @Schema(description = "Marka", example = "Apple")
        @NotBlank
        String brand,

        @Schema(description = "Model", example = "15 Pro")
        @NotBlank
        String model,

        @Schema(description = "Garanti süresi", example = "24 Ay")
        String warrantyPeriod,

        @Schema(description = "Teknik özellikler", example = "256GB, 8GB RAM")
        String specifications
) {
}