package com.demir.ecommerce.orderservice.dto.cart;

import java.util.List;

public record CartInternalResponse(
        Long cartId,
        Long userId,
        List<CartInternalItem> items
) {}
