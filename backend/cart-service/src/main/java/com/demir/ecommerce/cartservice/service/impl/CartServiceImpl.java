package com.demir.ecommerce.cartservice.service.impl;

import com.demir.ecommerce.cartservice.client.ProductServiceClient;
import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.dto.CartItemResponse;
import com.demir.ecommerce.cartservice.dto.CartResponse;
import com.demir.ecommerce.cartservice.dto.internal.CartInternalItem;
import com.demir.ecommerce.cartservice.dto.internal.CartInternalResponse;
import com.demir.ecommerce.cartservice.dto.product.ProductInternalResponse;
import com.demir.ecommerce.cartservice.entity.Cart;
import com.demir.ecommerce.cartservice.entity.CartItem;
import com.demir.ecommerce.cartservice.exception.message.CartErrorMessage;
import com.demir.ecommerce.cartservice.repository.CartRepository;
import com.demir.ecommerce.cartservice.service.CartService;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    public CartServiceImpl(CartRepository cartRepository,
                           ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.productServiceClient = productServiceClient;
    }

    @Override
    @Transactional
    public CartResponse getCart() {
        Long userId = SecurityUtils.getUserId();

        Cart cart = getOrCreateCart(userId);
        List<CartItem> cartItems = cart.getItems();

        if (cartItems.isEmpty()) {
            throw new BusinessException(CartErrorMessage.CART_IS_EMPTY);
        }

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .toList();

        List<ProductInternalResponse> products =
                productServiceClient.getProductsByIds(productIds);

        Map<Long, ProductInternalResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductInternalResponse::id, p -> p));

        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            ProductInternalResponse product = productMap.get(item.getProductId());

            if (product == null || !product.active()) {
                throw new BusinessException(CartErrorMessage.PRODUCT_NOT_AVAILABLE);
            }

            BigDecimal totalPrice =
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            cartTotal = cartTotal.add(totalPrice);

            items.add(new CartItemResponse(
                    item.getId(),
                    item.getProductId(),
                    product.name(),
                    product.imageUrl(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    totalPrice
            ));
        }

        return new CartResponse(cart.getId(), userId, items, cartTotal);
    }

    @Override
    @Transactional
    public void addToCart(AddToCartRequest request) {
        Long userId = SecurityUtils.getUserId();

        if (request.quantity() <= 0) {
            throw new BusinessException(CartErrorMessage.INVALID_QUANTITY);
        }

        Cart cart = getOrCreateCart(userId);
        List<CartItem> cartItems = cart.getItems();

        for (CartItem item : cartItems) {
            if (item.getProductId().equals(request.productId())) {
                item.setQuantity(item.getQuantity() + request.quantity());
                return;
            }
        }

        ProductInternalResponse product =
                productServiceClient.getProductsByIds(
                                Collections.singletonList(request.productId()))
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new BusinessException(CartErrorMessage.PRODUCT_NOT_AVAILABLE)
                        );

        if (!product.active()) {
            throw new BusinessException(CartErrorMessage.PRODUCT_NOT_AVAILABLE);
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProductId(product.id());
        newItem.setQuantity(request.quantity());
        newItem.setUnitPrice(product.price());

        cartItems.add(newItem);
    }

    @Override
    @Transactional
    public void clearCart() {
        Long userId = SecurityUtils.getUserId();

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() ->
                        new BusinessException(CartErrorMessage.CART_NOT_FOUND)
                );

        cart.getItems().clear();
    }
    @Override
    @Transactional
    public void clearCartInternal(Long userId) {

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() ->
                        new BusinessException(CartErrorMessage.CART_NOT_FOUND)
                );

        cart.getItems().clear();
    }


    @Override
    @Transactional(readOnly = true)
    public CartInternalResponse getInternalCart(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() ->
                        new BusinessException(CartErrorMessage.CART_NOT_FOUND)
                );

        return new CartInternalResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getItems().stream()
                        .map(i -> new CartInternalItem(
                                i.getProductId(),
                                i.getQuantity()
                        ))
                        .toList()
        );
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    return cartRepository.save(cart);
                });
    }
}