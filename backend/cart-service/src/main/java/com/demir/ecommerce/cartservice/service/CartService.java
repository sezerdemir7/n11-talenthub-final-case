package com.demir.ecommerce.cartservice.service;

import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.dto.CartResponse;
import com.demir.ecommerce.cartservice.dto.internal.CartInternalResponse;

import java.util.List;

public interface CartService {

    CartResponse getCart();

    void addToCart(AddToCartRequest request);

    void clearCart();

    CartInternalResponse getInternalCart(Long userId);
    void clearCartInternal(Long userId);
    void removeCartItemsInternal(Long userId, List<Long> productIds);

}
