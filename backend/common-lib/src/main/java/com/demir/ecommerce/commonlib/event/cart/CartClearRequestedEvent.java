package com.demir.ecommerce.commonlib.event.cart;

import java.util.List;

public record CartClearRequestedEvent(
        Long orderId,
        Long userId,
        List<Long> productIds
) {
}
