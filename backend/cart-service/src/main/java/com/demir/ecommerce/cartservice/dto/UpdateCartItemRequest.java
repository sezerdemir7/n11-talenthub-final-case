package com.demir.ecommerce.cartservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Request to update the quantity of a cart item.")
public record UpdateCartItemRequest(
        @Schema(
                description = "Quantity of product",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1"
        )
        Integer quantity
) {}
