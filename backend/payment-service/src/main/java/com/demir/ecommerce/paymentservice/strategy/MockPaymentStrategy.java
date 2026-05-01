package com.demir.ecommerce.paymentservice.strategy;

import com.demir.ecommerce.paymentservice.dto.PaymentRequest;
import com.demir.ecommerce.paymentservice.dto.PaymentResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(PaymentRequest request) {

        return new PaymentResult(
                true,
                "mock-" + UUID.randomUUID(),
                null
        );
    }

    @Override
    public PaymentResult refund(String paymentTransactionId, BigDecimal amount) {
        return new PaymentResult(
                true,
                "mock-refund-" + paymentTransactionId,
                null
        );
    }

}