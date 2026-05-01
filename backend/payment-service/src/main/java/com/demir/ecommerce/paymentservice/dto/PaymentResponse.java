package com.demir.ecommerce.paymentservice.dto;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        boolean success,
        String transactionId,
        String message
) {}