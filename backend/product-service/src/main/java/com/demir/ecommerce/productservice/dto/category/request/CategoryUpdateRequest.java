package com.demir.ecommerce.productservice.dto.category.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Kategori güncelleme isteği")
public record CategoryUpdateRequest(

        @Schema(description = "Kategori adı", example = "Elektronik")
        @NotBlank
        String name,

        @Schema(description = "Üst kategori ID", example = "1")
        Long parentId,

        @Schema(description = "Aktif mi?", example = "true")
        @NotNull
        Boolean active,

        @Schema(description = "Sıralama", example = "2")
        @Min(0)
        Integer sortOrder
) {
}