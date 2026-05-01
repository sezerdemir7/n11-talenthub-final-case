package com.demir.ecommerce.paymentservice.service;

import com.demir.ecommerce.paymentservice.dto.PaymentRequest;
import com.demir.ecommerce.paymentservice.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse pay(PaymentRequest request);
    void refund(Long orderId);

}
