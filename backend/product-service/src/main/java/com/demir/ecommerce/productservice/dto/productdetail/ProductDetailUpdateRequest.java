package com.demir.ecommerce.productservice.dto.productdetail;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ürün detay güncelleme isteği")
public record ProductDetailUpdateRequest(

        @Schema(description = "Kısa açıklama", example = "Güncellenmiş açıklama")
        String shortDescription,

        @Schema(description = "Uzun açıklama", example = "Detaylı yeni açıklama")
        String longDescription,

        @Schema(description = "Marka", example = "Apple")
        String brand,

        @Schema(description = "Model", example = "15 Pro Max")
        String model,

        @Schema(description = "Garanti süresi", example = "24 Ay")
        String warrantyPeriod,

        @Schema(description = "Teknik özellikler", example = "512GB")
        String specifications
) {
}