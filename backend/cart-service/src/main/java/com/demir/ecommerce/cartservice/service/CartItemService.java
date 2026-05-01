package com.demir.ecommerce.cartservice.service;

import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.entity.CartItem;

import java.util.List;

public interface CartItemService {

    CartItem addItem(Long cartId, AddToCartRequest request);

    void removeItem(Long cartId, Long productId);

    CartItem updateQuantity(Long cartId, Long productId, Integer quantity);

    List<CartItem> getItemsByCartId(Long cartId);

    void deleteAllByCartId(Long cartId);
}