package com.demir.ecommerce.orderservice.dto.payment;

public record PaymentResponse(
        boolean success,
        String transactionId,
        String message
) {}