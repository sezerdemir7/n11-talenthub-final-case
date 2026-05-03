package com.demir.ecommerce.paymentservice.unit.service.impl;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Unit Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStrategyFactory strategyFactory;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final BigDecimal AMOUNT = new BigDecimal("199.90");

    private PaymentRequest paymentRequest() {
        return new PaymentRequest(
                ORDER_ID, USER_ID, AMOUNT,
                new PaymentCardDto("John Doe", "5528790000000008", "12", "2030", "123")
        );
    }

    private Payment payment(PaymentStatus status) {
        Payment p = new Payment();
        p.setId(10L);
        p.setOrderId(ORDER_ID);
        p.setUserId(USER_ID);
        p.setAmount(AMOUNT);
        p.setStatus(status);
        p.setProvider(PaymentProvider.IYZICO);
        p.setTransactionId("txn_123");
        return p;
    }

    @Nested
    @DisplayName("pay()")
    class Pay {

        @Test
        @DisplayName("Should return existing payment response when payment already exists for order")
        void pay_existingPayment_returnsExistingResponse() {
            Payment existing = payment(PaymentStatus.SUCCESS);
            when(paymentRepository.findTopByOrderIdOrderByIdDesc(ORDER_ID))
                    .thenReturn(Optional.of(existing));

            PaymentResponse result = paymentService.pay(paymentRequest());

            assertThat(result.orderId()).isEqualTo(ORDER_ID);
            assertThat(result.success()).isTrue();
            assertThat(result.transactionId()).isEqualTo("txn_123");
            verifyNoInteractions(strategyFactory);
        }

        @Test
        @DisplayName("Should save payment with SUCCESS status when strategy succeeds")
        void pay_strategySucceeds_savesSuccessStatus() {
            when(paymentRepository.findTopByOrderIdOrderByIdDesc(ORDER_ID))
                    .thenReturn(Optional.empty());
            when(strategyFactory.get(any())).thenReturn(paymentStrategy);
            when(paymentStrategy.pay(any())).thenReturn(new PaymentResult(true, "txn_abc", null));
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });

            PaymentResponse result = paymentService.pay(paymentRequest());

            assertThat(result.success()).isTrue();
            assertThat(result.transactionId()).isEqualTo("txn_abc");
            verify(paymentRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("Should save payment with FAILED status when strategy fails")
        void pay_strategyFails_savesFailedStatus() {
            when(paymentRepository.findTopByOrderIdOrderByIdDesc(ORDER_ID))
                    .thenReturn(Optional.empty());
            when(strategyFactory.get(any())).thenReturn(paymentStrategy);
            when(paymentStrategy.pay(any())).thenReturn(new PaymentResult(false, null, "Insufficient funds"));
            when(paymentRepository.save(any())).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });

            PaymentResponse result = paymentService.pay(paymentRequest());

            assertThat(result.success()).isFalse();
            assertThat(result.message()).isEqualTo("Insufficient funds");
            verify(paymentRepository, times(2)).save(any());
        }
    }

    @Nested
    @DisplayName("refund()")
    class Refund {

        @Test
        @DisplayName("Should set status REFUNDED when refund succeeds")
        void refund_successfulRefund_setsRefundedStatus() {
            Payment p = payment(PaymentStatus.SUCCESS);
            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(p));
            when(strategyFactory.get(PaymentProvider.IYZICO)).thenReturn(paymentStrategy);
            when(paymentStrategy.refund("txn_123", AMOUNT))
                    .thenReturn(new PaymentResult(true, "refund_txn", null));
            when(paymentRepository.save(p)).thenReturn(p);

            assertThatNoException().isThrownBy(() -> paymentService.refund(ORDER_ID));

            assertThat(p.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            verify(paymentRepository).save(p);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when refund fails")
        void refund_failedRefund_throwsException() {
            Payment p = payment(PaymentStatus.SUCCESS);
            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(p));
            when(strategyFactory.get(PaymentProvider.IYZICO)).thenReturn(paymentStrategy);
            when(paymentStrategy.refund("txn_123", AMOUNT))
                    .thenReturn(new PaymentResult(false, null, "Refund failed"));
            when(paymentRepository.save(p)).thenReturn(p);

            assertThatThrownBy(() -> paymentService.refund(ORDER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Refund failed");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when no successful payment exists")
        void refund_noSuccessfulPayment_throwsException() {
            Payment p = payment(PaymentStatus.FAILED);
            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(p));

            assertThatThrownBy(() -> paymentService.refund(ORDER_ID))
                    .isInstanceOf(IllegalStateException.class);

            verifyNoInteractions(strategyFactory);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when no payment exists for order")
        void refund_noPayment_throwsException() {
            when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

            assertThatThrownBy(() -> paymentService.refund(ORDER_ID))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}