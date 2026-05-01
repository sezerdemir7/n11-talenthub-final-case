package com.demir.ecommerce.commonlib.event.stock;

public record StockReservationFailedEvent(
        Long orderId,
        Long userId,
        String reason
) {
}
