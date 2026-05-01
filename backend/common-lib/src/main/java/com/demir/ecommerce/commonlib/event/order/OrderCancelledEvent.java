package com.demir.ecommerce.commonlib.event.order;

import java.util.List;

public record OrderCancelledEvent(
        Long orderId,
        Long userId,
        String reason,
        List<OrderItemEvent> items
) {
}
