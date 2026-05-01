package com.demir.ecommerce.cartservice.dto.internal;

public record CartInternalItem(
        Long productId,
        Integer quantity
) {}