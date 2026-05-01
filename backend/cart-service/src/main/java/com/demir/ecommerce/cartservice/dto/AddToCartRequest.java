package com.demir.ecommerce.cartservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to add product to cart")
public record AddToCartRequest(

        @Schema(
                description = "Product ID to add to cart",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long productId,

        @Schema(
                description = "Quantity of product",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1"
        )
        Integer quantity

) {}