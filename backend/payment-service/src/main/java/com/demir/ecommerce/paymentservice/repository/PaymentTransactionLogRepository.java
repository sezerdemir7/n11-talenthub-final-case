package com.demir.ecommerce.paymentservice.repository;

import com.demir.ecommerce.paymentservice.entity.PaymentTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionLogRepository extends JpaRepository<PaymentTransactionLog, Long> {

    List<PaymentTransactionLog> findByPaymentId(Long paymentId);
}