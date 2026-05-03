package com.demir.ecommerce.orderservice.integration.messaging;

import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentSucceededEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservationFailedEvent;
import com.demir.ecommerce.orderservice.entity.*;
import com.demir.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.demir.ecommerce.orderservice.messaging.OrderSagaEventListener;
import com.demir.ecommerce.orderservice.repository.OrderItemRepository;
import com.demir.ecommerce.orderservice.repository.OrderRepository;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderSagaEventListener Integration Tests")
class OrderSagaEventListenerIntegrationTest {

    @Autowired
    private OrderSagaEventListener orderSagaEventListener;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private OrderEventPublisher orderEventPublisher;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;

    @AfterEach
    void cleanup() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    private Order savedOrder(OrderStatus status) {
        Order order = new Order();
        order.setUserId(USER_ID);
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal("199.90"));
        order.setCreatedAt(LocalDateTime.now());

        AddressEmbeddable address = new AddressEmbeddable();
        address.setCity("Istanbul");
        address.setDistrict("Kadikoy");
        address.setFullAddress("Test Address");
        address.setPostalCode("34700");
        order.setAddress(address);

        OrderItem item = new OrderItem();
        item.setProductId(PRODUCT_ID);
        item.setProductName("Test Product");
        item.setUnitPrice(new BigDecimal("99.95"));
        item.setQuantity(2);
        item.setOrder(order);

        order.setItems(new ArrayList<>(List.of(item)));
        return orderRepository.save(order);
    }

    @Nested
    @DisplayName("handlePaymentSucceeded()")
    class HandlePaymentSucceeded {

        @Test
        @DisplayName("Should persist CONFIRMED status and publish events when order is WAITING_PAYMENT")
        void handlePaymentSucceeded_waitingPayment_persistsConfirmedStatus() {
            Order order = savedOrder(OrderStatus.WAITING_PAYMENT);
            doNothing().when(orderEventPublisher).publishOrderCreated(any());
            doNothing().when(orderEventPublisher).publishCartClearRequested(any());

            orderSagaEventListener.handlePaymentSucceeded(
                    new PaymentSucceededEvent(order.getId(), USER_ID, new BigDecimal("199.90"), "txn_123")
            );

            Order fromDb = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            verify(orderEventPublisher).publishOrderCreated(any());
            verify(orderEventPublisher).publishCartClearRequested(any());
        }

        @Test
        @DisplayName("Should not change status when order is not found")
        void handlePaymentSucceeded_orderNotFound_doesNothing() {
            orderSagaEventListener.handlePaymentSucceeded(
                    new PaymentSucceededEvent(999L, USER_ID, new BigDecimal("199.90"), "txn_123")
            );

            verifyNoInteractions(orderEventPublisher);
        }

        @Test
        @DisplayName("Should not change status when order is already CONFIRMED")
        void handlePaymentSucceeded_alreadyConfirmed_doesNothing() {
            Order order = savedOrder(OrderStatus.CONFIRMED);

            orderSagaEventListener.handlePaymentSucceeded(
                    new PaymentSucceededEvent(order.getId(), USER_ID, new BigDecimal("199.90"), "txn_123")
            );

            Order fromDb = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            verifyNoInteractions(orderEventPublisher);
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed()")
    class HandlePaymentFailed {

        @Test
        @DisplayName("Should persist CANCELLED status when order is WAITING_PAYMENT")
        void handlePaymentFailed_waitingPayment_persistsCancelledStatus() {
            Order order = savedOrder(OrderStatus.WAITING_PAYMENT);

            orderSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(order.getId(), USER_ID, new BigDecimal("199.90"), "Insufficient funds", new ArrayList<>())
            );

            Order fromDb = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should not change status when order is not found")
        void handlePaymentFailed_orderNotFound_doesNothing() {
            orderSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(999L, USER_ID, new BigDecimal("199.90"), "Insufficient funds", new ArrayList<>())
            );

            assertThat(orderRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should not change status when order is already CONFIRMED")
        void handlePaymentFailed_alreadyConfirmed_doesNotChangeStatus() {
            Order order = savedOrder(OrderStatus.CONFIRMED);

            orderSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(order.getId(), USER_ID, new BigDecimal("199.90"), "Insufficient funds", new ArrayList<>())
            );

            Order fromDb = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("handleStockReservationFailed()")
    class HandleStockReservationFailed {

        @Test
        @DisplayName("Should persist CANCELLED status when stock reservation fails")
        void handleStockReservationFailed_existingOrder_persistsCancelledStatus() {
            Order order = savedOrder(OrderStatus.WAITING_PAYMENT);

            orderSagaEventListener.handleStockReservationFailed(
                    new StockReservationFailedEvent(order.getId(), USER_ID, "Out of stock")
            );

            Order fromDb = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should not change anything when order is not found")
        void handleStockReservationFailed_orderNotFound_doesNothing() {
            orderSagaEventListener.handleStockReservationFailed(
                    new StockReservationFailedEvent(999L, USER_ID, "Out of stock")
            );

            assertThat(orderRepository.findAll()).isEmpty();
        }
    }
}