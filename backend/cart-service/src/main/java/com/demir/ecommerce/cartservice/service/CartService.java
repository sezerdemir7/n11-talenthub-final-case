package com.demir.ecommerce.cartservice.service;

import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.dto.CartResponse;
import com.demir.ecommerce.cartservice.dto.internal.CartInternalResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    void addToCart(Long userId, AddToCartRequest request);

    void clearCart(Long userId);

    CartInternalResponse getInternalCart(Long userId);
}