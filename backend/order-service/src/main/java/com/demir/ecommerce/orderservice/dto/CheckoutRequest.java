package com.demir.ecommerce.orderservice.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull
        Long addressId
) {
}
