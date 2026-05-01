package com.demir.ecommerce.orderservice.client;

import com.demir.ecommerce.orderservice.dto.payment.PaymentRequest;
import com.demir.ecommerce.orderservice.dto.payment.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        path = "/internal/payments"
)
public interface PaymentServiceClient {

    @PostMapping
    PaymentResponse pay(@RequestBody PaymentRequest request);
}