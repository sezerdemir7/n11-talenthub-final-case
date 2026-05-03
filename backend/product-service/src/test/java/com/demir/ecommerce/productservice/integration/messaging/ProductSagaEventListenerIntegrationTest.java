package com.demir.ecommerce.productservice.integration.messaging;

import com.demir.ecommerce.commonlib.event.order.OrderCancelledEvent;
import com.demir.ecommerce.commonlib.event.order.OrderCreatedEvent;
import com.demir.ecommerce.commonlib.event.order.OrderExpiredEvent;
import com.demir.ecommerce.commonlib.event.order.OrderItemEvent;
import com.demir.ecommerce.commonlib.event.payment.PaymentFailedEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservationFailedEvent;
import com.demir.ecommerce.commonlib.event.stock.StockReservedEvent;
import com.demir.ecommerce.productservice.entity.Category;
import com.demir.ecommerce.productservice.entity.Product;
import com.demir.ecommerce.productservice.messaging.ProductSagaEventListener;
import com.demir.ecommerce.productservice.messaging.StockEventPublisher;
import com.demir.ecommerce.productservice.repository.CategoryRepository;
import com.demir.ecommerce.productservice.repository.ProductRepository;
import com.demir.ecommerce.productservice.service.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "aws.s3.bucket-name=test-bucket",
        "aws.s3.region=eu-central-1",
        "spring.cloud.aws.credentials.access-key=test",
        "spring.cloud.aws.credentials.secret-key=test"
})
@ActiveProfiles("test")
@DisplayName("ProductSagaEventListener Integration Tests")
class ProductSagaEventListenerIntegrationTest {

    @Autowired
    private ProductSagaEventListener productSagaEventListener;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private StockEventPublisher stockEventPublisher;

    @MockitoBean
    private StorageService storageService;

    private static final Long ORDER_ID = 10L;
    private static final Long USER_ID = 1L;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Category savedCategory() {
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setActive(true);
        category.setSortOrder(1);
        return categoryRepository.save(category);
    }

    private Product savedProduct(int stock, boolean active) {
        Product product = new Product();
        product.setSellerId(5L);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setPrice(new BigDecimal("99.90"));
        product.setStock(stock);
        product.setActive(active);
        product.setSuspendedBySellerStatus(false);
        product.setCategory(savedCategory());
        return productRepository.save(product);
    }

    private OrderItemEvent item(Long productId, int quantity) {
        return new OrderItemEvent(productId, "Test Product", new BigDecimal("99.90"), quantity);
    }

    @Nested
    @DisplayName("handleOrderCreated()")
    class HandleOrderCreated {

        @Test
        @DisplayName("Should decrease stock in database and publish StockReservedEvent")
        void handleOrderCreated_validStock_decreasesStockInDatabase() {
            Product product = savedProduct(10, true);

            productSagaEventListener.handleOrderCreated(
                    new OrderCreatedEvent(
                            ORDER_ID,
                            USER_ID,
                            new BigDecimal("199.80"),
                            List.of(item(product.getId(), 2))
                    )
            );

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getStock()).isEqualTo(8);
            verify(stockEventPublisher).publishStockReserved(any(StockReservedEvent.class));
            verify(stockEventPublisher, never()).publishStockReservationFailed(any());
        }

        @Test
        @DisplayName("Should publish failed event and keep stock when stock is insufficient")
        void handleOrderCreated_insufficientStock_publishesFailedAndKeepsStock() {
            Product product = savedProduct(1, true);

            productSagaEventListener.handleOrderCreated(
                    new OrderCreatedEvent(
                            ORDER_ID,
                            USER_ID,
                            new BigDecimal("199.80"),
                            List.of(item(product.getId(), 2))
                    )
            );

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getStock()).isEqualTo(1);
            verify(stockEventPublisher).publishStockReservationFailed(any(StockReservationFailedEvent.class));
            verify(stockEventPublisher, never()).publishStockReserved(any());
        }

        @Test
        @DisplayName("Should publish failed event when product is inactive")
        void handleOrderCreated_inactiveProduct_publishesFailedEvent() {
            Product product = savedProduct(10, false);

            productSagaEventListener.handleOrderCreated(
                    new OrderCreatedEvent(
                            ORDER_ID,
                            USER_ID,
                            new BigDecimal("199.80"),
                            List.of(item(product.getId(), 2))
                    )
            );

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getStock()).isEqualTo(10);
            verify(stockEventPublisher).publishStockReservationFailed(any(StockReservationFailedEvent.class));
            verify(stockEventPublisher, never()).publishStockReserved(any());
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed()")
    class HandlePaymentFailed {

        @Test
        @DisplayName("Should restore stock in database when payment fails")
        void handlePaymentFailed_existingProduct_restoresStockInDatabase() {
            Product product = savedProduct(8, true);

            productSagaEventListener.handlePaymentFailed(
                    new PaymentFailedEvent(
                            ORDER_ID,
                            USER_ID,
                            new BigDecimal("199.80"),
                            "Payment failed",
                            List.of(item(product.getId(), 2))
                    )
            );

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getStock()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("handleOrderExpired()")
    class HandleOrderExpired {

        @Test
        @DisplayName("Should restore stock in database when order expires")
        void handleOrderExpired_existingProduct_restoresStockInDatabase() {
            Product product = savedProduct(8, true);

            productSagaEventListener.handleOrderExpired(
                    new OrderExpiredEvent(ORDER_ID, USER_ID, List.of(item(product.getId(), 2)))
            );

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getStock()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("handleOrderCancelled()")
    class HandleOrderCancelled {

        @Test
        @DisplayName("Should restore stock in database when order is cancelled")
        void handleOrderCancelled_existingProduct_restoresStockInDatabase() {
            Product product = savedProduct(8, true);

            productSagaEventListener.handleOrderCancelled(
                    new OrderCancelledEvent(
                            ORDER_ID,
                            USER_ID,
                            "User cancelled",
                            List.of(item(product.getId(), 2))
                    )
            );

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getStock()).isEqualTo(10);
        }
    }
}
