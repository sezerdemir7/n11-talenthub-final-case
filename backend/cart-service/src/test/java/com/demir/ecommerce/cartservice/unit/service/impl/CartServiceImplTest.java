package com.demir.ecommerce.cartservice.unit.service.impl;

import com.demir.ecommerce.cartservice.client.ProductServiceClient;
import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.dto.CartItemResponse;
import com.demir.ecommerce.cartservice.dto.CartResponse;
import com.demir.ecommerce.cartservice.dto.internal.CartInternalResponse;
import com.demir.ecommerce.cartservice.dto.product.ProductInternalResponse;
import com.demir.ecommerce.cartservice.entity.Cart;
import com.demir.ecommerce.cartservice.entity.CartItem;
import com.demir.ecommerce.cartservice.repository.CartRepository;
import com.demir.ecommerce.cartservice.service.impl.CartServiceImpl;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Unit Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long CART_ID = 10L;


    private Cart emptyCart() {
        Cart cart = new Cart();
        cart.setId(CART_ID);
        cart.setUserId(USER_ID);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private CartItem cartItem(Long productId, int quantity, BigDecimal unitPrice) {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }

    private ProductInternalResponse activeProduct(Long id, int stock) {
        return new ProductInternalResponse(
                id, "Test Ürün", "http://img.test/1.jpg",
                new BigDecimal("99.90"), stock,true
        );
    }

    private ProductInternalResponse inactiveProduct(Long id) {
        return new ProductInternalResponse(
                id, "Pasif Ürün", null,
                new BigDecimal("50.00"),  10,false
        );
    }

    @Nested
    @DisplayName("getCart()")
    class GetCart {

        @Test
        @DisplayName("Should return zero total when cart is empty")
        void getCart_emptyCart_returnsZeroTotal() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(emptyCart()));

                CartResponse response = cartService.getCart();

                assertThat(response.items()).isEmpty();
                assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
                verifyNoInteractions(productServiceClient);
            }
        }

        @Test
        @DisplayName("Should create a new cart when none exists")
        void getCart_noCart_createsNewCart() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.empty());
                Cart newCart = emptyCart();
                when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

                CartResponse response = cartService.getCart();

                assertThat(response.items()).isEmpty();
                verify(cartRepository).save(any(Cart.class));
            }
        }

        @Test
        @DisplayName("Should return available=true when product is active and stock is sufficient")
        void getCart_availableProduct_returnsAvailableItem() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Cart cart = emptyCart();
                CartItem item = cartItem(PRODUCT_ID, 2, new BigDecimal("99.90"));
                cart.getItems().add(item);

                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(cart));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(PRODUCT_ID, 10)));

                CartResponse response = cartService.getCart();

                assertThat(response.items()).hasSize(1);
                CartItemResponse cartItemResponse = response.items().get(0);
                assertThat(cartItemResponse.available()).isTrue();
                assertThat(cartItemResponse.unavailableReason()).isNull();
                assertThat(response.totalPrice()).isEqualByComparingTo("199.80");
            }
        }

        @Test
        @DisplayName("Should return available=false with PRODUCT_NOT_FOUND reason when product does not exist")
        void getCart_productNotFound_returnsUnavailableItem() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Cart cart = emptyCart();
                cart.getItems().add(cartItem(PRODUCT_ID, 1, new BigDecimal("50.00")));

                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(cart));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(Collections.emptyList());

                CartResponse response = cartService.getCart();

                CartItemResponse ci = response.items().get(0);
                assertThat(ci.available()).isFalse();
                assertThat(ci.unavailableReason()).isEqualTo("PRODUCT_NOT_FOUND");
                assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            }
        }

        @Test
        @DisplayName("Should return available=false with PRODUCT_INACTIVE reason when product is inactive")
        void getCart_inactiveProduct_returnsUnavailableItem() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Cart cart = emptyCart();
                cart.getItems().add(cartItem(PRODUCT_ID, 1, new BigDecimal("50.00")));

                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(cart));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(inactiveProduct(PRODUCT_ID)));

                CartResponse response = cartService.getCart();

                CartItemResponse ci = response.items().get(0);
                assertThat(ci.available()).isFalse();
                assertThat(ci.unavailableReason()).isEqualTo("PRODUCT_INACTIVE");
            }
        }

        @Test
        @DisplayName("Should return available=false with INSUFFICIENT_STOCK reason when stock is not enough")
        void getCart_insufficientStock_returnsUnavailableItem() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Cart cart = emptyCart();
                cart.getItems().add(cartItem(PRODUCT_ID, 5, new BigDecimal("99.90")));

                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(cart));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(PRODUCT_ID, 2)));

                CartResponse response = cartService.getCart();

                CartItemResponse ci = response.items().get(0);
                assertThat(ci.available()).isFalse();
                assertThat(ci.unavailableReason()).isEqualTo("INSUFFICIENT_STOCK");
            }
        }
    }


    @Nested
    @DisplayName("addToCart()")
    class AddToCart {

        @Test
        @DisplayName("Should add item to cart when product is valid")
        void addToCart_validProduct_addsItem() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Cart cart = emptyCart();
                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(cart));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(PRODUCT_ID, 10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 3));

                assertThat(cart.getItems()).hasSize(1);
                assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
            }
        }

        @Test
        @DisplayName("Should update quantity when same product is added again")
        void addToCart_existingProduct_updatesQuantity() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Cart cart = emptyCart();
                CartItem existing = cartItem(PRODUCT_ID, 2, new BigDecimal("99.90"));
                existing.setCart(cart);
                cart.getItems().add(existing);

                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(cart));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(PRODUCT_ID, 10)));

                cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 3));


                assertThat(cart.getItems()).hasSize(1);
                assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when quantity is zero or negative")
        void addToCart_invalidQuantity_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                assertThatThrownBy(() ->
                        cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 0))
                ).isInstanceOf(BusinessException.class);

                assertThatThrownBy(() ->
                        cartService.addToCart(new AddToCartRequest(PRODUCT_ID, -1))
                ).isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when product is not found")
        void addToCart_productNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(emptyCart()));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(Collections.emptyList());

                assertThatThrownBy(() ->
                        cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 1))
                ).isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when product is inactive")
        void addToCart_inactiveProduct_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(emptyCart()));
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(inactiveProduct(PRODUCT_ID)));

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
                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(emptyCart()));
                // Stokta 2 var, 5 istiyor
                when(productServiceClient.getProductsByIds(anyList()))
                        .thenReturn(List.of(activeProduct(PRODUCT_ID, 2)));

                assertThatThrownBy(() ->
                        cartService.addToCart(new AddToCartRequest(PRODUCT_ID, 5))
                ).isInstanceOf(BusinessException.class);
            }
        }
    }



    @Nested
    @DisplayName("clearCart()")
    class ClearCart {

        @Test
        @DisplayName("Should clear all items from cart")
        void clearCart_existingCart_clearsItems() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);

                Cart cart = emptyCart();
                cart.getItems().add(cartItem(PRODUCT_ID, 2, new BigDecimal("99.90")));

                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.of(cart));

                cartService.clearCart();

                assertThat(cart.getItems()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is not found")
        void clearCart_notFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(cartRepository.findByUserIdWithItems(USER_ID))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() -> cartService.clearCart())
                        .isInstanceOf(BusinessException.class);
            }
        }
    }


    @Nested
    @DisplayName("clearCartInternal()")
    class ClearCartInternal {

        @Test
        @DisplayName("Should clear cart for given userId")
        void clearCartInternal_existingCart_clearsItems() {
            Cart cart = emptyCart();
            cart.getItems().add(cartItem(PRODUCT_ID, 1, BigDecimal.TEN));
            when(cartRepository.findByUserIdWithItems(USER_ID))
                    .thenReturn(Optional.of(cart));

            cartService.clearCartInternal(USER_ID);

            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is not found")
        void clearCartInternal_notFound_throwsException() {
            when(cartRepository.findByUserIdWithItems(USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.clearCartInternal(USER_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("removeCartItemsInternal()")
    class RemoveCartItemsInternal {

        @Test
        @DisplayName("Should remove only the specified products from cart")
        void removeCartItemsInternal_existingItems_removesCorrectItems() {
            Long productIdToRemove = 200L;
            Long productIdToKeep = 300L;

            Cart cart = emptyCart();
            cart.getItems().add(cartItem(productIdToRemove, 1, BigDecimal.TEN));
            cart.getItems().add(cartItem(productIdToKeep, 2, BigDecimal.TEN));

            when(cartRepository.findByUserIdWithItems(USER_ID))
                    .thenReturn(Optional.of(cart));

            cartService.removeCartItemsInternal(USER_ID, List.of(productIdToRemove));

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getItems().get(0).getProductId()).isEqualTo(productIdToKeep);
        }

        @Test
        @DisplayName("Should do nothing when productIds is null or empty")
        void removeCartItemsInternal_emptyList_doesNothing() {
            cartService.removeCartItemsInternal(USER_ID, Collections.emptyList());
            cartService.removeCartItemsInternal(USER_ID, null);

            verifyNoInteractions(cartRepository);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is not found")
        void removeCartItemsInternal_cartNotFound_throwsException() {
            when(cartRepository.findByUserIdWithItems(USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    cartService.removeCartItemsInternal(USER_ID, List.of(PRODUCT_ID))
            ).isInstanceOf(BusinessException.class);
        }
    }


    @Nested
    @DisplayName("getInternalCart()")
    class GetInternalCart {

        @Test
        @DisplayName("Should return CartInternalResponse with cart contents")
        void getInternalCart_existingCart_returnsResponse() {
            Cart cart = emptyCart();
            cart.getItems().add(cartItem(PRODUCT_ID, 3, BigDecimal.TEN));
            when(cartRepository.findByUserIdWithItems(USER_ID))
                    .thenReturn(Optional.of(cart));

            CartInternalResponse response = cartService.getInternalCart(USER_ID);

            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.cartId()).isEqualTo(CART_ID);
            assertThat(response.items()).hasSize(1);
            assertThat(response.items().get(0).productId()).isEqualTo(PRODUCT_ID);
            assertThat(response.items().get(0).quantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is not found")
        void getInternalCart_notFound_throwsException() {
            when(cartRepository.findByUserIdWithItems(USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.getInternalCart(USER_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
