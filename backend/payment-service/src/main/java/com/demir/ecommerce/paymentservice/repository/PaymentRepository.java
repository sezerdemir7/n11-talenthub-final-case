package com.demir.ecommerce.paymentservice.repository;

import com.demir.ecommerce.paymentservice.entity.Payment;
import com.demir.ecommerce.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findTopByOrderIdOrderByIdDesc(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);


}
