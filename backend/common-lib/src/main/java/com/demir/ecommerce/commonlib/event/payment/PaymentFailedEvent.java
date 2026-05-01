package com.demir.ecommerce.commonlib.event.payment;

import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;

import java.math.BigDecimal;
import java.util.List;

public record PaymentFailedEvent(
        Long orderId,
        Long userId,
        BigDecimal amount,
        String reason,
        List<OrderItemEvent> items
) {
}
