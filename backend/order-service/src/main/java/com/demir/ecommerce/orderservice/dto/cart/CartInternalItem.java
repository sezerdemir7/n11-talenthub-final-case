package com.demir.ecommerce.orderservice.dto.cart;

public record CartInternalItem(
        Long productId,
        Integer quantity
) {}