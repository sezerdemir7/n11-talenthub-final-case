package com.demir.ecommerce.paymentservice.dto;

public record PaymentResult(
        boolean success,
        String transactionId,
        String failureReason
) {}