package com.demir.ecommerce.paymentservice.unit.messaging;

import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.paymentservice.messaging.PaymentRefundEventListener;
import com.demir.ecommerce.paymentservice.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRefundEventListener Unit Tests")
class PaymentRefundEventListenerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentRefundEventListener paymentRefundEventListener;

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 42L;

    private OrderCancelledEvent cancelledEvent() {
        return new OrderCancelledEvent(ORDER_ID, USER_ID, "Order cancelled by user", List.of());
    }

    @Nested
    @DisplayName("handleOrderCancelled()")
    class HandleOrderCancelled {

        @Test
        @DisplayName("Should call paymentService.refund when order cancelled event received")
        void handleOrderCancelled_validEvent_callsRefund() {
            doNothing().when(paymentService).refund(ORDER_ID);

            paymentRefundEventListener.handleOrderCancelled(cancelledEvent());

            verify(paymentService).refund(ORDER_ID);
        }

        @Test
        @DisplayName("Should not propagate exception when refund throws RuntimeException")
        void handleOrderCancelled_refundFails_doesNotThrow() {
            doThrow(new IllegalStateException("Refund failed")).when(paymentService).refund(ORDER_ID);

            assertThatCode(() -> paymentRefundEventListener.handleOrderCancelled(cancelledEvent()))
                    .doesNotThrowAnyException();
        }
    }
}