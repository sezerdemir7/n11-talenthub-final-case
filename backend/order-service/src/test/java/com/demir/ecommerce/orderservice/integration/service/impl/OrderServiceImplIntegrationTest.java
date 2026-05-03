package com.demir.ecommerce.orderservice.integration.service.impl;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.orderservice.client.CartServiceClient;
import com.demir.ecommerce.orderservice.client.ProductServiceClient;
import com.demir.ecommerce.orderservice.client.UserServiceClient;
import com.demir.ecommerce.orderservice.dto.CheckoutRequest;
import com.demir.ecommerce.orderservice.dto.OrderResponse;
import com.demir.ecommerce.orderservice.dto.cart.CartInternalItem;
import com.demir.ecommerce.orderservice.dto.cart.CartInternalResponse;
import com.demir.ecommerce.orderservice.dto.product.ProductInternalResponse;
import com.demir.ecommerce.orderservice.dto.user.AddressInternalResponse;
import com.demir.ecommerce.orderservice.entity.*;
import com.demir.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.demir.ecommerce.orderservice.repository.OrderItemRepository;
import com.demir.ecommerce.orderservice.repository.OrderRepository;
import com.demir.ecommerce.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderServiceImpl Integration Tests")
class OrderServiceImplIntegrationTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private CartServiceClient cartServiceClient;

    @MockitoBean
    private ProductServiceClient productServiceClient;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @MockitoBean
    private OrderEventPublisher orderEventPublisher;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long ADDRESS_ID = 5L;

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

    private ProductInternalResponse activeProduct() {
        return new ProductInternalResponse(PRODUCT_ID, "Test Product", null, new BigDecimal("99.95"), 10, true);
    }

    private CartInternalResponse cart() {
        return new CartInternalResponse(1L, USER_ID, List.of(new CartInternalItem(PRODUCT_ID, 2)));
    }

    private AddressInternalResponse address() {
        return new AddressInternalResponse(ADDRESS_ID, USER_ID, "Home", "Istanbul", "Kadikoy", "Test Address", "34700", true);
    }

    @Nested
    @DisplayName("getOrderById()")
    class GetOrderById {

        @Test
        @DisplayName("Should return order from database when owner requests")
        void getOrderById_ownerRequest_returnsOrderFromDatabase() {
            Order order = savedOrder(OrderStatus.CONFIRMED);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(true);

                OrderResponse result = orderService.getOrderById(order.getId());

                assertThat(result).isNotNull();
                assertThat(result.orderId()).isEqualTo(order.getId());
                assertThat(result.userId()).isEqualTo(USER_ID);
                assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED.name());
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when order does not exist in database")
        void getOrderById_notFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                assertThatThrownBy(() -> orderService.getOrderById(999L))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not owner or admin")
        void getOrderById_notOwner_throwsAccessDenied() {
            Order order = savedOrder(OrderStatus.CONFIRMED);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(false);

                assertThatThrownBy(() -> orderService.getOrderById(order.getId()))
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("getOrdersByUserId()")
    class GetOrdersByUserId {

        @Test
        @DisplayName("Should return paginated orders from database")
        void getOrdersByUserId_withOrders_returnsPaginatedOrders() {
            savedOrder(OrderStatus.CONFIRMED);
            savedOrder(OrderStatus.CANCELLED);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                PageResponse<OrderResponse> result = orderService.getOrdersByUserId(0, 10);

                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getTotalElements()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Should exclude EXPIRED orders from results")
        void getOrdersByUserId_excludesExpiredOrders() {
            savedOrder(OrderStatus.CONFIRMED);
            savedOrder(OrderStatus.EXPIRED);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                PageResponse<OrderResponse> result = orderService.getOrdersByUserId(0, 10);

                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.CONFIRMED.name());
            }
        }

        @Test
        @DisplayName("Should return empty page when user has no orders")
        void getOrdersByUserId_noOrders_returnsEmptyPage() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                PageResponse<OrderResponse> result = orderService.getOrdersByUserId(0, 10);

                assertThat(result.getContent()).isEmpty();
                assertThat(result.getTotalElements()).isEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("Should update order status to CANCELLED in database and publish event")
        void cancelOrder_confirmedOrder_persistsCancelledStatus() {
            Order order = savedOrder(OrderStatus.CONFIRMED);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(true);
                doNothing().when(orderEventPublisher).publishOrderCancelled(any());

                orderService.cancelOrder(order.getId());

                Order fromDb = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                verify(orderEventPublisher).publishOrderCancelled(any());
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when order does not exist in database")
        void cancelOrder_notFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                assertThatThrownBy(() -> orderService.cancelOrder(999L))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when order is not CONFIRMED")
        void cancelOrder_notConfirmed_throwsException() {
            Order order = savedOrder(OrderStatus.WAITING_PAYMENT);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(true);

                assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
                        .isInstanceOf(BusinessException.class);

                Order fromDb = orderRepository.findById(order.getId()).orElseThrow();
                assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.WAITING_PAYMENT);
            }
        }
    }

    @Nested
    @DisplayName("checkout()")
    class Checkout {

        @Test
        @DisplayName("Should persist order with WAITING_PAYMENT status to database")
        void checkout_validRequest_persistsOrderToDatabase() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                when(cartServiceClient.getCart(USER_ID)).thenReturn(cart());
                when(productServiceClient.getByIds(anyList())).thenReturn(List.of(activeProduct()));
                when(userServiceClient.getAddress(USER_ID, ADDRESS_ID)).thenReturn(address());

                CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID));
                OrderResponse result = orderService.checkout(request);

                assertThat(result).isNotNull();
                assertThat(result.status()).isEqualTo(OrderStatus.WAITING_PAYMENT.name());

                Optional<Order> fromDb = orderRepository.findWithItemsById(result.orderId());
                assertThat(fromDb).isPresent();
                assertThat(fromDb.get().getItems()).hasSize(1);
                assertThat(fromDb.get().getItems().get(0).getProductId()).isEqualTo(PRODUCT_ID);
                assertThat(fromDb.get().getTotalPrice()).isEqualByComparingTo("199.90");
            }
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist when cart is empty")
        void checkout_emptyCart_doesNotPersist() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(new CartInternalResponse(1L, USER_ID, List.of()));

                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID))))
                        .isInstanceOf(BusinessException.class);

                assertThat(orderRepository.findAll()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist when product is inactive")
        void checkout_inactiveProduct_doesNotPersist() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(cart());
                when(productServiceClient.getByIds(anyList())).thenReturn(List.of(
                        new ProductInternalResponse(PRODUCT_ID, "Test", null, new BigDecimal("99.95"), 10, false)
                ));

                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID))))
                        .isInstanceOf(BusinessException.class);

                assertThat(orderRepository.findAll()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist when stock is insufficient")
        void checkout_insufficientStock_doesNotPersist() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(cart());
                when(productServiceClient.getByIds(anyList())).thenReturn(List.of(
                        new ProductInternalResponse(PRODUCT_ID, "Test", null, new BigDecimal("99.95"), 1, true)
                ));

                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID))))
                        .isInstanceOf(BusinessException.class);

                assertThat(orderRepository.findAll()).isEmpty();
            }
        }
    }
}