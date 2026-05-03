package com.demir.ecommerce.orderservice.unit.messaging;


import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentSucceededEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservationFailedEvent;
import com.demir.ecommerce.orderservice.entity.Order;
import com.demir.ecommerce.orderservice.entity.OrderItem;
import com.demir.ecommerce.orderservice.entity.OrderStatus;
import com.demir.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.demir.ecommerce.orderservice.messaging.OrderSagaEventListener;
import com.demir.ecommerce.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderSagaEventListener Unit Tests")
class OrderSagaEventListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderSagaEventListener orderSagaEventListener;

    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 42L;

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal("199.90"));

        OrderItem item = new OrderItem();
        item.setProductId(100L);
        item.setProductName("Test Product");
        item.setUnitPrice(new BigDecimal("99.95"));
        item.setQuantity(2);
        item.setOrder(order);

        order.setItems(new ArrayList<>(List.of(item)));
        return order;
    }

    @Nested
    @DisplayName("handlePaymentSucceeded()")
    class HandlePaymentSucceeded {

        @Test
        @DisplayName("Should set status CONFIRMED and publish events when order is WAITING_PAYMENT")
        void handlePaymentSucceeded_waitingPayment_confirmsAndPublishesEvents() {
            Order order = order(OrderStatus.WAITING_PAYMENT);
            when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            orderSagaEventListener.handlePaymentSucceeded(new PaymentSucceededEvent(ORDER_ID, USER_ID, new BigDecimal("199.90"), "txn_123"));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            verify(orderRepository).save(order);
            verify(orderEventPublisher).publishOrderCreated(any());
            verify(orderEventPublisher).publishCartClearRequested(any());
        }

        @Test
        @DisplayName("Should do nothing when order is not found")
        void handlePaymentSucceeded_orderNotFound_doesNothing() {
            when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.empty());

            orderSagaEventListener.handlePaymentSucceeded(new PaymentSucceededEvent(ORDER_ID, USER_ID, new BigDecimal("199.90"), "txn_123"));

            verify(orderRepository, never()).save(any());
            verifyNoInteractions(orderEventPublisher);
        }

        @Test
        @DisplayName("Should do nothing when order status is not WAITING_PAYMENT")
        void handlePaymentSucceeded_notWaitingPayment_doesNothing() {
            Order order = order(OrderStatus.CONFIRMED);
            when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(order));

            orderSagaEventListener.handlePaymentSucceeded(new PaymentSucceededEvent(ORDER_ID, USER_ID, new BigDecimal("199.90"), "txn_123"));

            verify(orderRepository, never()).save(any());
            verifyNoInteractions(orderEventPublisher);
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed()")
    class HandlePaymentFailed {

        @Test
        @DisplayName("Should set status CANCELLED when order is WAITING_PAYMENT")
        void handlePaymentFailed_waitingPayment_cancelsOrder() {
            Order order = order(OrderStatus.WAITING_PAYMENT);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            orderSagaEventListener.handlePaymentFailed(new PaymentFailedEvent(ORDER_ID, USER_ID, new BigDecimal("199.90"),"Insufficient funds", new ArrayList<>()));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Should do nothing when order is not found")
        void handlePaymentFailed_orderNotFound_doesNothing() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            orderSagaEventListener.handlePaymentFailed(new PaymentFailedEvent(ORDER_ID, USER_ID, new BigDecimal("199.90"),"Insufficient funds", new ArrayList<>()));

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should do nothing when order status is not WAITING_PAYMENT")
        void handlePaymentFailed_notWaitingPayment_doesNothing() {
            Order order = order(OrderStatus.CONFIRMED);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            orderSagaEventListener.handlePaymentFailed(new PaymentFailedEvent(ORDER_ID, USER_ID, new BigDecimal("199.90"),"Insufficient funds", new ArrayList<>()));

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("handleStockReservationFailed()")
    class HandleStockReservationFailed {

        @Test
        @DisplayName("Should set status CANCELLED when stock reservation fails")
        void handleStockReservationFailed_existingOrder_cancelsOrder() {
            Order order = order(OrderStatus.WAITING_PAYMENT);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            orderSagaEventListener.handleStockReservationFailed(new StockReservationFailedEvent(ORDER_ID, USER_ID, "Out of stock"));

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("Should do nothing when order is not found")
        void handleStockReservationFailed_orderNotFound_doesNothing() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            orderSagaEventListener.handleStockReservationFailed(new StockReservationFailedEvent(ORDER_ID, USER_ID, "Out of stock"));

            verify(orderRepository, never()).save(any());
        }
    }
}