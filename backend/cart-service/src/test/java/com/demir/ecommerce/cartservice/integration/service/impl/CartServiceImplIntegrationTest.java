package com.demir.ecommerce.cartservice.integration.service.impl;

import com.demir.ecommerce.cartservice.client.ProductServiceClient;
import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.dto.CartResponse;
import com.demir.ecommerce.cartservice.dto.internal.CartInternalResponse;
import com.demir.ecommerce.cartservice.dto.product.ProductInternalResponse;
import com.demir.ecommerce.cartservice.entity.Cart;
import com.demir.ecommerce.cartservice.repository.CartRepository;
import com.demir.ecommerce.cartservice.service.impl.CartServiceImpl;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CartServiceImpl Integration Tests")
class CartServiceImplIntegrationTest {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private CartRepository cartRepository;


    @MockitoBean
    private ProductServiceClient productServiceClient;

    private static final Long USER_ID = 42L;
    private static final Long PRODUCT_ID = 100L;

    @AfterEach
    void cleanup() {
        cartRepository.findByUserIdWithItems(USER_ID)
                .ifPresent(cart -> {
                    cart.getItems().clear();
                    cartRepository.delete(cart);
                });
    }

    private ProductInternalResponse activeProduct(int stock) {
        return new ProductInternalResponse(
                PRODUCT_ID, "Test Product", "http://img.test/1.jpg",
                new BigDecimal("99.90"), stock, true
        );
    }

    private ProductInternalResponse inactiveProduct() {
        return new ProductInternalResponse(
                PRODUCT_ID, "Inactive Product", null,
                new BigDecimal("50.00"), 10, false
        );
    }

    @Nested
    @DisplayName("addToCart()")
    class AddToCart {

        @Test
        @DisplayName("Should persist cart and item to database when product is valid")
        void addToCart_validProduct_persistsToDatabase() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 2));

                Optional<Cart> saved = cartRepository.findByUserIdWithItems(USER_ID);
                assertThat(saved).isPresent();
                assertThat(saved.get().getItems()).hasSize(1);
                assertThat(saved.get().getItems().get(0).getProductId()).isEqualTo(PRODUCT_ID);
                assertThat(saved.get().getItems().get(0).getQuantity()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Should accumulate quantity in database when same product added twice")
        void addToCart_sameProductTwice_accumulatesQuantityInDatabase() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 2));
                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 3));

                Optional<Cart> saved = cartRepository.findByUserIdWithItems(USER_ID);
                assertThat(saved).isPresent();
                assertThat(saved.get().getItems()).hasSize(1);
                assertThat(saved.get().getItems().get(0).getQuantity()).isEqualTo(5);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist anything when quantity is zero")
        void addToCart_zeroQuantity_doesNotPersist() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                assertThatThrownBy(() ->
                        cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 0))
                ).isInstanceOf(BusinessException.class);

                assertThat(cartRepository.findByUserIdWithItems(USER_ID)).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when product is inactive")
        void addToCart_inactiveProduct_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(inactiveProduct()));

                assertThatThrownBy(() ->
                        cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 1))
                ).isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when stock is insufficient")
        void addToCart_insufficientStock_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(1)));

                assertThatThrownBy(() ->
                        cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 5))
                ).isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("getCart()")
    class GetCart {

        @Test
        @DisplayName("Should return empty cart response when no items exist")
        void getCart_noItems_returnsEmptyResponse() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                CartResponse response = cartService.getCart();

                assertThat(response.items()).isEmpty();
                assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            }
        }

        @Test
        @DisplayName("Should return correct total price for available items")
        void getCart_withItems_returnsCorrectTotal() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 3));

                CartResponse response = cartService.getCart();

                assertThat(response.items()).hasSize(1);
                assertThat(response.items().get(0).available()).isTrue();
                // 99.90 * 3 = 299.70
                assertThat(response.totalPrice()).isEqualByComparingTo("299.70");
            }
        }

        @Test
        @DisplayName("Should mark item as unavailable and exclude from total when product is not found")
        void getCart_productNotFoundInService_marksItemUnavailable() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)));
                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 2));

                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(Collections.emptyList());

                CartResponse response = cartService.getCart();

                assertThat(response.items()).hasSize(1);
                assertThat(response.items().get(0).available()).isFalse();
                assertThat(response.items().get(0).unavailableReason()).isEqualTo("PRODUCT_NOT_FOUND");
                assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            }
        }
    }

    @Nested
    @DisplayName("clearCart()")
    class ClearCart {

        @Test
        @DisplayName("Should remove all items from database")
        void clearCart_withItems_removesAllFromDatabase() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 2));
                cartService.clearCart();

                Optional<Cart> cart = cartRepository.findByUserIdWithItems(USER_ID);
                assertThat(cart).isPresent();
                assertThat(cart.get().getItems()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when cart does not exist in database")
        void clearCart_noCart_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                assertThatThrownBy(() -> cartService.clearCart())
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("clearCartInternal()")
    class ClearCartInternal {

        @Test
        @DisplayName("Should remove all items from database for given userId")
        void clearCartInternal_withItems_removesAllFromDatabase() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 1));
                cartService.clearCartInternal(USER_ID);

                Optional<Cart> cart = cartRepository.findByUserIdWithItems(USER_ID);
                assertThat(cart).isPresent();
                assertThat(cart.get().getItems()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when cart does not exist in database")
        void clearCartInternal_noCart_throwsException() {
            assertThatThrownBy(() -> cartService.clearCartInternal(USER_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("removeCartItemsInternal()")
    class RemoveCartItemsInternal {

        @Test
        @DisplayName("Should remove only specified products from database")
        void removeCartItemsInternal_multipleItems_removesOnlySpecified() {
            Long otherProductId = 200L;

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                ProductInternalResponse otherProduct = new ProductInternalResponse(
                        otherProductId, "Other Product", null,
                        new BigDecimal("20.00"), 5, true
                );

                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)))
                        .thenReturn(List.of(otherProduct));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 1));
                cartService.addToCart(new AddToCartRequest(otherProductId, 2));

                cartService.removeCartItemsInternal(USER_ID, List.of(PRODUCT_ID));

                Optional<Cart> cart = cartRepository.findByUserIdWithItems(USER_ID);
                assertThat(cart).isPresent();
                assertThat(cart.get().getItems()).hasSize(1);
                assertThat(cart.get().getItems().get(0).getProductId()).isEqualTo(otherProductId);
            }
        }

        @Test
        @DisplayName("Should not interact with database when productIds list is empty")
        void removeCartItemsInternal_emptyList_doesNothing() {
            assertThatNoException().isThrownBy(() ->
                    cartService.removeCartItemsInternal(USER_ID, Collections.emptyList())
            );
        }
    }

    @Nested
    @DisplayName("getInternalCart()")
    class GetInternalCart {

        @Test
        @DisplayName("Should return CartInternalResponse with persisted items")
        void getInternalCart_withItems_returnsPersistedData() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 4));

                CartInternalResponse response = cartService.getInternalCart(USER_ID);

                assertThat(response.userId()).isEqualTo(USER_ID);
                assertThat(response.items()).hasSize(1);
                assertThat(response.items().get(0).productId()).isEqualTo(PRODUCT_ID);
                assertThat(response.items().get(0).quantity()).isEqualTo(4);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when cart does not exist in database")
        void getInternalCart_noCart_throwsException() {
            assertThatThrownBy(() -> cartService.getInternalCart(USER_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }
}