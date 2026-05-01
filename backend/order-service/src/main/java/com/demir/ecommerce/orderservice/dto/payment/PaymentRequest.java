package com.demir.ecommerce.orderservice.dto.payment;

import java.math.BigDecimal;

public record PaymentRequest(
        Long orderId,
        Long userId,
        BigDecimal amount
) {}
