package com.demir.ecommerce.paymentservice.service.impl;

import com.demir.ecommerce.paymentservice.dto.PaymentRequest;
import com.demir.ecommerce.paymentservice.dto.PaymentResponse;
import com.demir.ecommerce.paymentservice.dto.PaymentResult;
import com.demir.ecommerce.paymentservice.entity.Payment;
import com.demir.ecommerce.paymentservice.entity.PaymentProvider;
import com.demir.ecommerce.paymentservice.entity.PaymentStatus;
import com.demir.ecommerce.paymentservice.repository.PaymentRepository;
import com.demir.ecommerce.paymentservice.service.PaymentService;
import com.demir.ecommerce.paymentservice.strategy.PaymentStrategy;
import com.demir.ecommerce.paymentservice.strategy.PaymentStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStrategyFactory strategyFactory;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentStrategyFactory strategyFactory) {
        this.paymentRepository = paymentRepository;
        this.strategyFactory = strategyFactory;
    }


    @Override
    @Transactional
    public PaymentResponse pay(PaymentRequest request) {

        Payment existingPayment = paymentRepository
                .findTopByOrderIdOrderByIdDesc(request.orderId())
                .orElse(null);

        if (existingPayment != null) {
            return toResponse(existingPayment);
        }

        PaymentProvider provider = PaymentProvider.IYZICO;

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProvider(provider);
        payment.setCreatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        PaymentStrategy strategy = strategyFactory.get(provider);

        PaymentResult result = strategy.pay(request);

        if (result.success()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(result.transactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
        }

        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        return toResponse(payment);
    }

    @Override
    @Transactional
    public void refund(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Successful payment not found for order: " + orderId
                ));

        PaymentStrategy strategy = strategyFactory.get(payment.getProvider());

        PaymentResult result = strategy.refund(
                payment.getTransactionId(),
                payment.getAmount()
        );

        if (result.success()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            return;
        }

        payment.setFailureReason(result.failureReason());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        throw new IllegalStateException(result.failureReason());
    }


    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus() == PaymentStatus.SUCCESS,
                payment.getTransactionId(),
                payment.getFailureReason()
        );
    }
}
