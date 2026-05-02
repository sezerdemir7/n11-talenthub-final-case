package com.demir.ecommerce.notificationservice.dto;

import java.math.BigDecimal;

public record OrderNotificationData(
        Long orderId,
        BigDecimal totalPrice
) {
}