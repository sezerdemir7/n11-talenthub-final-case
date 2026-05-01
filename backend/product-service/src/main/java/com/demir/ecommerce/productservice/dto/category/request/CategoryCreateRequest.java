package com.demir.ecommerce.productservice.dto.category.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Kategori oluşturma isteği")
public record CategoryCreateRequest(

        @Schema(description = "Kategori adı", example = "Elektronik")
        @NotBlank
        String name,

        @Schema(description = "Üst kategori ID", example = "0")
        Long parentId,

        @Schema(description = "Aktif mi?", example = "true")
        @NotNull
        Boolean active,

        @Schema(description = "Sıralama", example = "1")
        @Min(0)
        Integer sortOrder
) {
}