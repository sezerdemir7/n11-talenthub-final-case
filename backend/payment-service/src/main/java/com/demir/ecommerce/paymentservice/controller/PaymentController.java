package com.demir.ecommerce.paymentservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentSucceededEvent;
import com.demir.ecommerce.paymentservice.dto.PaymentRequest;
import com.demir.ecommerce.paymentservice.dto.PaymentResponse;
import com.demir.ecommerce.paymentservice.messaging.PaymentEventPublisher;
import com.demir.ecommerce.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentController(PaymentService paymentService,
                             PaymentEventPublisher paymentEventPublisher) {
        this.paymentService = paymentService;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    @PostMapping("/pay")
    public ResponseEntity<RestResponse<PaymentResponse>> pay(
            @Valid @RequestBody PaymentRequest request
    ) {
        PaymentResponse response = paymentService.pay(request);

        if (response.success()) {
            paymentEventPublisher.publishPaymentSucceeded(
                    new PaymentSucceededEvent(
                            response.orderId(),
                            request.userId(),
                            request.amount(),
                            response.transactionId()
                    )
            );
        } else {
            paymentEventPublisher.publishPaymentFailed(
                    new PaymentFailedEvent(
                            response.orderId(),
                            request.userId(),
                            request.amount(),
                            response.message(),
                            null
                    )
            );
        }

        return ResponseEntity.ok(
                RestResponse.of(response)
        );
    }
}
