package com.demir.ecommerce.commonlib.event.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal totalPrice,
        List<OrderItemEvent> items
) {
}
