package com.demir.ecommerce.paymentservice.strategy;

import com.demir.ecommerce.paymentservice.dto.PaymentRequest;
import com.demir.ecommerce.paymentservice.dto.PaymentResult;

import java.math.BigDecimal;

public interface PaymentStrategy {

    PaymentResult pay(PaymentRequest request);

    PaymentResult refund(String paymentTransactionId, BigDecimal amount);
}
