package com.demir.ecommerce.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request to create an order")
public record CreateOrderRequest(

        @Schema(
                description = "Items in the order",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<OrderItemRequest> items,

        @Schema(
                description = "Delivery address for the order",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        AddressDto address
) {}