package com.demir.ecommerce.paymentservice.integration.service.impl;

import com.demir.ecommerce.paymentservice.dto.PaymentCardDto;
import com.demir.ecommerce.paymentservice.dto.PaymentRequest;
import com.demir.ecommerce.paymentservice.dto.PaymentResponse;
import com.demir.ecommerce.paymentservice.dto.PaymentResult;
import com.demir.ecommerce.paymentservice.entity.Payment;
import com.demir.ecommerce.paymentservice.entity.PaymentProvider;
import com.demir.ecommerce.paymentservice.entity.PaymentStatus;
import com.demir.ecommerce.paymentservice.repository.PaymentRepository;
import com.demir.ecommerce.paymentservice.service.impl.PaymentServiceImpl;
import com.demir.ecommerce.paymentservice.strategy.PaymentStrategy;
import com.demir.ecommerce.paymentservice.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PaymentServiceImpl Integration Tests")
class PaymentServiceImplIntegrationTest {

    @Autowired
    private PaymentServiceImpl paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentStrategyFactory strategyFactory;

    @MockitoBean
    private PaymentStrategy paymentStrategy;

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final BigDecimal AMOUNT = new BigDecimal("199.90");

    @AfterEach
    void cleanup() {
        paymentRepository.deleteAll();
    }

    private PaymentRequest paymentRequest() {
        return new PaymentRequest(
                ORDER_ID, USER_ID, AMOUNT,
                new PaymentCardDto("John Doe", "5528790000000008", "12", "2030", "123")
        );
    }

    private Payment savedPayment(PaymentStatus status, String transactionId) {
        Payment p = new Payment();
        p.setOrderId(ORDER_ID);
        p.setUserId(USER_ID);
        p.setAmount(AMOUNT);
        p.setStatus(status);
        p.setProvider(PaymentProvider.IYZICO);
        p.setTransactionId(transactionId);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(p);
    }

    @Nested
    @DisplayName("pay()")
    class Pay {

        @Test
        @DisplayName("Should persist payment with SUCCESS status when strategy succeeds")
        void pay_strategySucceeds_persistsSuccessPayment() {
            when(strategyFactory.get(any())).thenReturn(paymentStrategy);
            when(paymentStrategy.pay(any())).thenReturn(new PaymentResult(true, "txn_abc", null));

            PaymentResponse result = paymentService.pay(paymentRequest());

            assertThat(result.success()).isTrue();
            assertThat(result.transactionId()).isEqualTo("txn_abc");

            Optional<Payment> fromDb = paymentRepository.findTopByOrderIdOrderByIdDesc(ORDER_ID);
            assertThat(fromDb).isPresent();
            assertThat(fromDb.get().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(fromDb.get().getTransactionId()).isEqualTo("txn_abc");
        }

        @Test
        @DisplayName("Should persist payment with FAILED status when strategy fails")
        void pay_strategyFails_persistsFailedPayment() {
            when(strategyFactory.get(any())).thenReturn(paymentStrategy);
            when(paymentStrategy.pay(any())).thenReturn(new PaymentResult(false, null, "Insufficient funds"));

            PaymentResponse result = paymentService.pay(paymentRequest());

            assertThat(result.success()).isFalse();
            assertThat(result.message()).isEqualTo("Insufficient funds");

            Optional<Payment> fromDb = paymentRepository.findTopByOrderIdOrderByIdDesc(ORDER_ID);
            assertThat(fromDb).isPresent();
            assertThat(fromDb.get().getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("Should return existing payment without calling strategy when payment already exists")
        void pay_existingPayment_returnsExistingWithoutCallingStrategy() {
            savedPayment(PaymentStatus.SUCCESS, "txn_existing");

            PaymentResponse result = paymentService.pay(paymentRequest());

            assertThat(result.success()).isTrue();
            assertThat(result.transactionId()).isEqualTo("txn_existing");
            verifyNoInteractions(strategyFactory);
        }

        @Test
        @DisplayName("Should persist exactly one payment per new order")
        void pay_newOrder_persistsExactlyOnePayment() {
            when(strategyFactory.get(any())).thenReturn(paymentStrategy);
            when(paymentStrategy.pay(any())).thenReturn(new PaymentResult(true, "txn_new", null));

            paymentService.pay(paymentRequest());

            List<Payment> payments = paymentRepository.findByOrderId(ORDER_ID);
            assertThat(payments).hasSize(1);
        }
    }

    @Nested
    @DisplayName("refund()")
    class Refund {

        @Test
        @DisplayName("Should persist REFUNDED status when refund succeeds")
        void refund_successfulRefund_persistsRefundedStatus() {
            Payment p = savedPayment(PaymentStatus.SUCCESS, "txn_123");
            when(strategyFactory.get(PaymentProvider.IYZICO)).thenReturn(paymentStrategy);
            when(paymentStrategy.refund("txn_123", AMOUNT))
                    .thenReturn(new PaymentResult(true, "refund_txn", null));

            assertThatNoException().isThrownBy(() -> paymentService.refund(ORDER_ID));

            Payment fromDb = paymentRepository.findById(p.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("Should not change status and throw exception when refund fails")
        void refund_failedRefund_doesNotChangeStatusAndThrows() {
            Payment p = savedPayment(PaymentStatus.SUCCESS, "txn_123");
            when(strategyFactory.get(PaymentProvider.IYZICO)).thenReturn(paymentStrategy);
            when(paymentStrategy.refund("txn_123", AMOUNT))
                    .thenReturn(new PaymentResult(false, null, "Refund failed"));

            assertThatThrownBy(() -> paymentService.refund(ORDER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Refund failed");

            Payment fromDb = paymentRepository.findById(p.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when no successful payment exists in database")
        void refund_noSuccessfulPayment_throwsException() {
            savedPayment(PaymentStatus.FAILED, null);

            assertThatThrownBy(() -> paymentService.refund(ORDER_ID))
                    .isInstanceOf(IllegalStateException.class);

            verifyNoInteractions(strategyFactory);
        }
    }
}