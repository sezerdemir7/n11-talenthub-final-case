package com.demir.ecommerce.cartservice.unit.service.impl;

import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.entity.Cart;
import com.demir.ecommerce.cartservice.entity.CartItem;
import com.demir.ecommerce.cartservice.repository.CartItemRepository;
import com.demir.ecommerce.cartservice.repository.CartRepository;
import com.demir.ecommerce.cartservice.service.impl.CartItemServiceImpl;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartItemServiceImpl Unit Tests")
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private static final Long CART_ID = 1L;
    private static final Long PRODUCT_ID = 100L;

    private Cart cart() {
        Cart cart = new Cart();
        cart.setId(CART_ID);
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private CartItem cartItem() {
        CartItem item = new CartItem();
        item.setId(10L);
        item.setProductId(PRODUCT_ID);
        item.setQuantity(2);
        item.setCart(cart());

        return item;
    }

    @Nested
    @DisplayName("addItem()")
    class AddItem {

        @Test
        @DisplayName("Should save and return cart item when request is valid")
        void addItem_validRequest_savesAndReturnsItem() {
            Cart cart = cart();
            AddToCartRequest request = new AddToCartRequest(PRODUCT_ID, 3);
            CartItem saved = cartItem();

            when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(saved);

            CartItem result = cartItemService.addItem(CART_ID, request);

            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(PRODUCT_ID);
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when quantity is zero")
        void addItem_zeroQuantity_throwsException() {
            assertThatThrownBy(() ->
                    cartItemService.addItem(CART_ID, new AddToCartRequest(PRODUCT_ID, 0))
            ).isInstanceOf(BusinessException.class);

            verifyNoInteractions(cartRepository, cartItemRepository);
        }

        @Test
        @DisplayName("Should throw BusinessException when quantity is negative")
        void addItem_negativeQuantity_throwsException() {
            assertThatThrownBy(() ->
                    cartItemService.addItem(CART_ID, new AddToCartRequest(PRODUCT_ID, -5))
            ).isInstanceOf(BusinessException.class);

            verifyNoInteractions(cartRepository, cartItemRepository);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is not found")
        void addItem_cartNotFound_throwsException() {
            when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    cartItemService.addItem(CART_ID, new AddToCartRequest(PRODUCT_ID, 2))
            ).isInstanceOf(BusinessException.class);

            verifyNoInteractions(cartItemRepository);
        }
    }

    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("Should delete cart item when it exists")
        void removeItem_existingItem_deletesItem() {
            CartItem item = cartItem();
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                    .thenReturn(Optional.of(item));

            cartItemService.removeItem(CART_ID, PRODUCT_ID);

            verify(cartItemRepository).delete(item);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart item is not found")
        void removeItem_itemNotFound_throwsException() {
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    cartItemService.removeItem(CART_ID, PRODUCT_ID)
            ).isInstanceOf(BusinessException.class);

            verify(cartItemRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("updateQuantity()")
    class UpdateQuantity {

        @Test
        @DisplayName("Should update and return item when request is valid")
        void updateQuantity_validQuantity_updatesAndReturnsItem() {
            CartItem item = cartItem();
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                    .thenReturn(Optional.of(item));
            when(cartItemRepository.save(item)).thenReturn(item);

            CartItem result = cartItemService.updateQuantity(CART_ID, PRODUCT_ID, 5);

            assertThat(result.getQuantity()).isEqualTo(5);
            verify(cartItemRepository).save(item);
        }

        @Test
        @DisplayName("Should throw BusinessException when quantity is zero")
        void updateQuantity_zeroQuantity_throwsException() {
            assertThatThrownBy(() ->
                    cartItemService.updateQuantity(CART_ID, PRODUCT_ID, 0)
            ).isInstanceOf(BusinessException.class);

            verifyNoInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("Should throw BusinessException when quantity is negative")
        void updateQuantity_negativeQuantity_throwsException() {
            assertThatThrownBy(() ->
                    cartItemService.updateQuantity(CART_ID, PRODUCT_ID, -3)
            ).isInstanceOf(BusinessException.class);

            verifyNoInteractions(cartItemRepository);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart item is not found")
        void updateQuantity_itemNotFound_throwsException() {
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    cartItemService.updateQuantity(CART_ID, PRODUCT_ID, 3)
            ).isInstanceOf(BusinessException.class);

            verify(cartItemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getItemsByCartId()")
    class GetItemsByCartId {

        @Test
        @DisplayName("Should return all items belonging to the cart")
        void getItemsByCartId_existingItems_returnsItemList() {
            List<CartItem> items = List.of(cartItem(), cartItem());
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(items);

            List<CartItem> result = cartItemService.getItemsByCartId(CART_ID);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when cart has no items")
        void getItemsByCartId_noItems_returnsEmptyList() {
            when(cartItemRepository.findAllByCartId(CART_ID)).thenReturn(List.of());

            List<CartItem> result = cartItemService.getItemsByCartId(CART_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteAllByCartId()")
    class DeleteAllByCartId {

        @Test
        @DisplayName("Should clear all items and save cart")
        void deleteAllByCartId_existingCart_clearsItemsAndSaves() {
            Cart cart = cart();
            cart.getItems().add(cartItem());

            when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

            cartItemService.deleteAllByCartId(CART_ID);

            assertThat(cart.getItems()).isEmpty();
            verify(cartRepository).save(cart);
        }

        @Test
        @DisplayName("Should throw BusinessException when cart is not found")
        void deleteAllByCartId_cartNotFound_throwsException() {
            when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    cartItemService.deleteAllByCartId(CART_ID)
            ).isInstanceOf(BusinessException.class);

            verify(cartRepository, never()).save(any());
        }
    }
}