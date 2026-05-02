package com.demir.ecommerce.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CheckoutRequest(
        @NotNull
        Long addressId,

        @NotEmpty
        List<Long> selectedProductIds
) {
}
