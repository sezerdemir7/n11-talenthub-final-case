package com.demir.ecommerce.commonlib.event.order;

import java.util.List;

public record OrderExpiredEvent(
        Long orderId,
        Long userId,
        List<OrderItemEvent> items
) {
}
