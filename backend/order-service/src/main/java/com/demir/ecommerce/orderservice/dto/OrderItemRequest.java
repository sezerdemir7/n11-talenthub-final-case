package com.demir.ecommerce.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single product item in order request")
public record OrderItemRequest(

        @Schema(description = "Product ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productId,

        @Schema(description = "Quantity", example = "2", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
        Integer quantity
) {}