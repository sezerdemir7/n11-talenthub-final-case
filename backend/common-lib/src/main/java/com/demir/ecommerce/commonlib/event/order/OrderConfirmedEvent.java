package com.demir.ecommerce.commonlib.event.order;

public record OrderConfirmedEvent(
        Long orderId,
        Long userId
) {
}
