package com.demir.ecommerce.commonlib.event.cart;

public record CartClearRequestedEvent(
        Long orderId,
        Long userId
) {
}
