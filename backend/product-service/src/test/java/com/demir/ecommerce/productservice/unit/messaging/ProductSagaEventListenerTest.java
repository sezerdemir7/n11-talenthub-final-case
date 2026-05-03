package com.demir.ecommerce.productservice.unit.messaging;

import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCreatedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderExpiredEvent;
import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservationFailedEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservedEvent;
import com.demir.ecommerce.productservice.entity.Product;
import com.demir.ecommerce.productservice.messaging.ProductSagaEventListener;
import com.demir.ecommerce.productservice.messaging.StockEventPublisher;
import com.demir.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSagaEventListener Unit Tests")
class ProductSagaEventListenerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockEventPublisher stockEventPublisher;

    @InjectMocks
    private ProductSagaEventListener productSagaEventListener;

    private static final Long ORDER_ID = 10L;
    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;

    private Product product(int stock, boolean active) {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setName("Test Product");
        product.setStock(stock);
        product.setActive(active);
        return product;
    }

    private OrderItemEvent item(int quantity) {
        return new OrderItemEvent(PRODUCT_ID, "Test Product", new BigDecimal("99.90"), quantity);
    }

    private OrderCreatedEvent orderCreatedEvent(int quantity) {
        return new OrderCreatedEvent(
                ORDER_ID,
                USER_ID,
                new BigDecimal("199.80"),
                List.of(item(quantity))
        );
    }

    @Nested
    @DisplayName("handleOrderCreated()")
    class HandleOrderCreated {

        @Test
        @DisplayName("Should reserve stock and publish StockReservedEvent")
        void handleOrderCreated_validStock_reservesStockAndPublishesEvent() {
            Product product = product(10, true);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            productSagaEventListener.handleOrderCreated(orderCreatedEvent(2));

            assertThat(product.getStock()).isEqualTo(8);
            verify(productRepository).save(product);
            verify(stockEventPublisher).publishStockReserved(any(StockReservedEvent.class));
            verify(stockEventPublisher, never()).publishStockReservationFailed(any());
        }

        @Test
        @DisplayName("Should publish StockReservationFailedEvent when product is not found")
        void handleOrderCreated_productNotFound_publishesFailedEvent() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            productSagaEventListener.handleOrderCreated(orderCreatedEvent(2));

            ArgumentCaptor<StockReservationFailedEvent> captor =
                    ArgumentCaptor.forClass(StockReservationFailedEvent.class);

            verify(stockEventPublisher).publishStockReservationFailed(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(ORDER_ID);
            assertThat(captor.getValue().userId()).isEqualTo(USER_ID);
            assertThat(captor.getValue().reason()).contains("Product not found");
            verify(stockEventPublisher, never()).publishStockReserved(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should publish StockReservationFailedEvent when product is inactive")
        void handleOrderCreated_inactiveProduct_publishesFailedEvent() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product(10, false)));

            productSagaEventListener.handleOrderCreated(orderCreatedEvent(2));

            ArgumentCaptor<StockReservationFailedEvent> captor =
                    ArgumentCaptor.forClass(StockReservationFailedEvent.class);

            verify(stockEventPublisher).publishStockReservationFailed(captor.capture());
            assertThat(captor.getValue().reason()).contains("Product inactive");
            verify(stockEventPublisher, never()).publishStockReserved(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should publish StockReservationFailedEvent when stock is insufficient")
        void handleOrderCreated_insufficientStock_publishesFailedEvent() {
            Product product = product(1, true);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            productSagaEventListener.handleOrderCreated(orderCreatedEvent(2));

            ArgumentCaptor<StockReservationFailedEvent> captor =
                    ArgumentCaptor.forClass(StockReservationFailedEvent.class);

            verify(stockEventPublisher).publishStockReservationFailed(captor.capture());
            assertThat(captor.getValue().reason()).contains("Insufficient stock");
            assertThat(product.getStock()).isEqualTo(1);
            verify(stockEventPublisher, never()).publishStockReserved(any());
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed()")
    class HandlePaymentFailed {

        @Test
        @DisplayName("Should restore stock when payment fails")
        void handlePaymentFailed_existingProduct_restoresStock() {
            Product product = product(8, true);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            productSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(
                            ORDER_ID,
                            USER_ID,
                            new BigDecimal("199.80"),
                            "Payment failed",
                            List.of(item(2))
                    )
            );

            assertThat(product.getStock()).isEqualTo(10);
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should skip missing product")
        void handlePaymentFailed_productNotFound_doesNothing() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            productSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(
                            ORDER_ID,
                            USER_ID,
                            new BigDecimal("199.80"),
                            "Payment failed",
                            List.of(item(2))
                    )
            );

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should do nothing when items is null or empty")
        void handlePaymentFailed_nullOrEmptyItems_doesNothing() {
            productSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(ORDER_ID, USER_ID, BigDecimal.TEN, "Failed", null)
            );
            productSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(ORDER_ID, USER_ID, BigDecimal.TEN, "Failed", List.of())
            );

            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("handleOrderExpired()")
    class HandleOrderExpired {

        @Test
        @DisplayName("Should restore stock when order expires")
        void handleOrderExpired_existingProduct_restoresStock() {
            Product product = product(8, true);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            productSagaEventListener.handleOrderExpired(
                    new OrderExpiredEvent(ORDER_ID, USER_ID, List.of(item(2)))
            );

            assertThat(product.getStock()).isEqualTo(10);
            verify(productRepository).save(product);
        }
    }

    @Nested
    @DisplayName("handleOrderCancelled()")
    class HandleOrderCancelled {

        @Test
        @DisplayName("Should restore stock when order is cancelled")
        void handleOrderCancelled_existingProduct_restoresStock() {
            Product product = product(8, true);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

            productSagaEventListener.handleOrderCancelled(
                    new OrderCancelledEvent(ORDER_ID, USER_ID, "User cancelled", List.of(item(2)))
            );

            assertThat(product.getStock()).isEqualTo(10);
            verify(productRepository).save(product);
        }
    }
}
