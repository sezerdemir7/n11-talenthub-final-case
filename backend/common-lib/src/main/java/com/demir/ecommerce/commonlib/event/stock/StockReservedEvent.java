package com.demir.ecommerce.commonlib.event.stock;

import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;

import java.math.BigDecimal;
import java.util.List;

public record StockReservedEvent(
        Long orderId,
        Long userId,
        BigDecimal amount,
        List<OrderItemEvent> items
) {
}
