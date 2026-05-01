package com.demir.ecommerce.commonlib.event.payment;

import java.math.BigDecimal;

public record PaymentSucceededEvent(
        Long orderId,
        Long userId,
        BigDecimal amount,
        String transactionId
) {
}
