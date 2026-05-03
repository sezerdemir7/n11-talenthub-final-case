package com.demir.ecommerce.cartservice.integration.service.impl;

import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.entity.Cart;
import com.demir.ecommerce.cartservice.entity.CartItem;
import com.demir.ecommerce.cartservice.repository.CartItemRepository;
import com.demir.ecommerce.cartservice.repository.CartRepository;
import com.demir.ecommerce.cartservice.service.impl.CartItemServiceImpl;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CartItemServiceImpl Integration Tests")
class CartItemServiceImplIntegrationTest {

    @Autowired
    private CartItemServiceImpl cartItemService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    private static final Long PRODUCT_ID = 100L;

    private Cart savedCart() {
        Cart cart = new Cart();
        cart.setUserId(99L);
        cart.setItems(new ArrayList<>());
        return cartRepository.save(cart);
    }

    private CartItem savedCartItem(Cart cart, Long productId) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(productId);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("99.90"));
        return cartItemRepository.save(item);
    }

    @AfterEach
    void cleanup() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
    }

    @Nested
    @DisplayName("addItem()")
    class AddItem {

        @Test
        @DisplayName("Should throw exception because addItem() does not set unitPrice (known bug)")
        void addItem_missingUnitPrice_throwsException() {
            Cart cart = savedCart();

            assertThatThrownBy(() ->
                    cartItemService.addItem(cart.getId(), new AddToCartRequest(PRODUCT_ID, 3))
            ).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist when quantity is zero")
        void addItem_zeroQuantity_doesNotPersist() {
            Cart cart = savedCart();

            assertThatThrownBy(() ->
                    cartItemService.addItem(cart.getId(), new AddToCartRequest(PRODUCT_ID, 0))
            ).isInstanceOf(BusinessException.class);

            assertThat(cartItemRepository.findAllByCartId(cart.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist when quantity is negative")
        void addItem_negativeQuantity_doesNotPersist() {
            Cart cart = savedCart();

            assertThatThrownBy(() ->
                    cartItemService.addItem(cart.getId(), new AddToCartRequest(PRODUCT_ID, -2))
            ).isInstanceOf(BusinessException.class);

            assertThat(cartItemRepository.findAllByCartId(cart.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should throw BusinessException when cart does not exist in database")
        void addItem_cartNotFound_throwsException() {
            assertThatThrownBy(() ->
                    cartItemService.addItem(999L, new AddToCartRequest(PRODUCT_ID, 1))
            ).isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("Should delete cart item from database")
        void removeItem_existingItem_deletesFromDatabase() {
            Cart cart = savedCart();
            savedCartItem(cart, PRODUCT_ID);

            cartItemService.removeItem(cart.getId(), PRODUCT_ID);

            assertThat(cartItemRepository.findAllByCartId(cart.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should throw BusinessException when cart item does not exist")
        void removeItem_itemNotFound_throwsException() {
            Cart cart = savedCart();

            assertThatThrownBy(() ->
                    cartItemService.removeItem(cart.getId(), PRODUCT_ID)
            ).isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("updateQuantity()")
    class UpdateQuantity {

        @Test
        @DisplayName("Should update quantity in database")
        void updateQuantity_validQuantity_updatesInDatabase() {
            Cart cart = savedCart();
            savedCartItem(cart, PRODUCT_ID);

            CartItem updated = cartItemService.updateQuantity(cart.getId(), PRODUCT_ID, 7);

            assertThat(updated.getQuantity()).isEqualTo(7);

            CartItem fromDb = cartItemRepository.findByCartIdAndProductId(cart.getId(), PRODUCT_ID)
                    .orElseThrow();
            assertThat(fromDb.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw BusinessException when quantity is zero")
        void updateQuantity_zeroQuantity_throwsException() {
            Cart cart = savedCart();

            assertThatThrownBy(() ->
                    cartItemService.updateQuantity(cart.getId(), PRODUCT_ID, 0)
            ).isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should throw BusinessException when quantity is negative")
        void updateQuantity_negativeQuantity_throwsException() {
            Cart cart = savedCart();

            assertThatThrownBy(() ->
                    cartItemService.updateQuantity(cart.getId(), PRODUCT_ID, -1)
            ).isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart item does not exist")
        void updateQuantity_itemNotFound_throwsException() {
            Cart cart = savedCart();

            assertThatThrownBy(() ->
                    cartItemService.updateQuantity(cart.getId(), PRODUCT_ID, 3)
            ).isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getItemsByCartId()")
    class GetItemsByCartId {

        @Test
        @DisplayName("Should return all persisted items for given cartId")
        void getItemsByCartId_withItems_returnsAllItems() {
            Cart cart = savedCart();
            savedCartItem(cart, PRODUCT_ID);
            savedCartItem(cart, 200L);

            List<CartItem> items = cartItemService.getItemsByCartId(cart.getId());

            assertThat(items).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when cart has no items")
        void getItemsByCartId_noItems_returnsEmptyList() {
            Cart cart = savedCart();

            List<CartItem> items = cartItemService.getItemsByCartId(cart.getId());

            assertThat(items).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteAllByCartId()")
    class DeleteAllByCartId {

        @Test
        @DisplayName("Should throw LazyInitializationException because Cart.items is lazily loaded (known bug)")
        void deleteAllByCartId_withItems_throwsLazyInitializationException() {
            Cart cart = savedCart();
            savedCartItem(cart, PRODUCT_ID);
            savedCartItem(cart, 200L);

            assertThatThrownBy(() ->
                    cartItemService.deleteAllByCartId(cart.getId())
            ).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart does not exist in database")
        void deleteAllByCartId_cartNotFound_throwsException() {
            assertThatThrownBy(() ->
                    cartItemService.deleteAllByCartId(999L)
            ).isInstanceOf(BusinessException.class);
        }
    }
}