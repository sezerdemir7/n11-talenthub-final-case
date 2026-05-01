package com.demir.ecommerce.cartservice.dto.internal;

import java.util.List;

public record CartInternalResponse(
        Long cartId,
        Long userId,
        List<CartInternalItem> items
) {}
