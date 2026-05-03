package com.demir.ecommerce.orderservice.unit.service.impl;

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
import com.demir.ecommerce.orderservice.entity.Order;
import com.demir.ecommerce.orderservice.entity.OrderItem;
import com.demir.ecommerce.orderservice.entity.OrderStatus;
import com.demir.ecommerce.orderservice.messaging.OrderEventPublisher;
import com.demir.ecommerce.orderservice.repository.OrderRepository;
import com.demir.ecommerce.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartServiceClient cartServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long USER_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long ADDRESS_ID = 5L;

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setStatus(status);
        order.setTotalPrice(new BigDecimal("199.90"));

        OrderItem item = new OrderItem();
        item.setProductId(PRODUCT_ID);
        item.setProductName("Test Product");
        item.setUnitPrice(new BigDecimal("99.95"));
        item.setQuantity(2);
        item.setOrder(order);

        order.setItems(new ArrayList<>(List.of(item)));

        com.demir.ecommerce.orderservice.entity.AddressEmbeddable address =
                new com.demir.ecommerce.orderservice.entity.AddressEmbeddable();
        address.setCity("Istanbul");
        address.setDistrict("Kadikoy");
        address.setFullAddress("Test Address");
        address.setPostalCode("34700");
        order.setAddress(address);

        return order;
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
        @DisplayName("Should return order response when owner requests")
        void getOrderById_ownerRequest_returnsOrderResponse() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(true);
                when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.CONFIRMED)));

                OrderResponse result = orderService.getOrderById(ORDER_ID);

                assertThat(result).isNotNull();
                assertThat(result.orderId()).isEqualTo(ORDER_ID);
                assertThat(result.userId()).isEqualTo(USER_ID);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when order is not found")
        void getOrderById_orderNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not owner or admin")
        void getOrderById_notOwner_throwsAccessDenied() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(false);
                when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.CONFIRMED)));

                assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID))
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("getOrdersByUserId()")
    class GetOrdersByUserId {

        @Test
        @DisplayName("Should return paginated orders for user")
        void getOrdersByUserId_withOrders_returnsPaginatedResponse() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Page<Order> page = new PageImpl<>(List.of(order(OrderStatus.CONFIRMED)));
                when(orderRepository.findAllByUserIdAndStatusNot(eq(USER_ID), eq(OrderStatus.EXPIRED), any(Pageable.class)))
                        .thenReturn(page);

                PageResponse<OrderResponse> result = orderService.getOrdersByUserId(0, 10);

                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getTotalElements()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("Should return empty page when user has no orders")
        void getOrdersByUserId_noOrders_returnsEmptyPage() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Page<Order> emptyPage = new PageImpl<>(List.of());
                when(orderRepository.findAllByUserIdAndStatusNot(eq(USER_ID), eq(OrderStatus.EXPIRED), any(Pageable.class)))
                        .thenReturn(emptyPage);

                PageResponse<OrderResponse> result = orderService.getOrdersByUserId(0, 10);

                assertThat(result.getContent()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("Should cancel order and publish event when status is CONFIRMED")
        void cancelOrder_confirmedOrder_cancelsAndPublishesEvent() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(true);
                Order order = order(OrderStatus.CONFIRMED);
                when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(order));
                when(orderRepository.save(order)).thenReturn(order);

                orderService.cancelOrder(ORDER_ID);

                assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
                verify(orderRepository).save(order);
                verify(orderEventPublisher).publishOrderCancelled(any());
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when order is not found")
        void cancelOrder_orderNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not owner or admin")
        void cancelOrder_notOwner_throwsAccessDenied() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(false);
                when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.CONFIRMED)));

                assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID))
                        .isInstanceOf(BusinessException.class);

                verify(orderRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when order status is not CONFIRMED")
        void cancelOrder_notConfirmedStatus_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(USER_ID)).thenReturn(true);
                when(orderRepository.findWithItemsById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.WAITING_PAYMENT)));

                assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID))
                        .isInstanceOf(BusinessException.class);

                verify(orderRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("checkout()")
    class Checkout {

        @Test
        @DisplayName("Should create order and return response when all inputs are valid")
        void checkout_validRequest_createsOrderAndReturnsResponse() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                when(cartServiceClient.getCart(USER_ID)).thenReturn(cart());
                when(productServiceClient.getByIds(anyList())).thenReturn(List.of(activeProduct()));
                when(userServiceClient.getAddress(USER_ID, ADDRESS_ID)).thenReturn(address());
                when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    o.setId(ORDER_ID);
                    return o;
                });

                CheckoutRequest request = new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID));
                OrderResponse result = orderService.checkout(request);

                assertThat(result).isNotNull();
                assertThat(result.status()).isEqualTo(OrderStatus.WAITING_PAYMENT.name());
                verify(orderRepository).save(any(Order.class));
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is empty")
        void checkout_emptyCart_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(new CartInternalResponse(1L, USER_ID, List.of()));

                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID))))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is null")
        void checkout_nullCart_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(null);

                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID))))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when selected product is not in cart")
        void checkout_productNotInCart_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(cart());

                // Sepette PRODUCT_ID var ama farklı bir ID seçiliyor
                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(999L))))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when product is inactive")
        void checkout_inactiveProduct_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(cart());

                ProductInternalResponse inactive = new ProductInternalResponse(
                        PRODUCT_ID, "Test Product", null, new BigDecimal("99.95"), 10, false
                );
                when(productServiceClient.getByIds(anyList())).thenReturn(List.of(inactive));

                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID))))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when stock is insufficient")
        void checkout_insufficientStock_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartServiceClient.getCart(USER_ID)).thenReturn(cart());

                // Stokta 1 var, sepette 2 adet seçili
                ProductInternalResponse lowStock = new ProductInternalResponse(
                        PRODUCT_ID, "Test Product", null, new BigDecimal("99.95"), 1, true
                );
                when(productServiceClient.getByIds(anyList())).thenReturn(List.of(lowStock));

                assertThatThrownBy(() -> orderService.checkout(new CheckoutRequest(ADDRESS_ID, List.of(PRODUCT_ID))))
                        .isInstanceOf(BusinessException.class);
            }
        }
    }
}