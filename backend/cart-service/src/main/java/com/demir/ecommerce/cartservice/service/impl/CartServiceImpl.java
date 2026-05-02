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

    private static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    private static final String PRODUCT_INACTIVE = "PRODUCT_INACTIVE";
    private static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";

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
            String unavailableReason = getUnavailableReason(product, item.getQuantity());
            boolean available = unavailableReason == null;

            BigDecimal unitPrice = product != null ? product.price() : item.getUnitPrice();
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            if (available) {
                cartTotal = cartTotal.add(totalPrice);
            }

            items.add(new CartItemResponse(
                    item.getId(),
                    item.getProductId(),
                    product != null ? product.name() : "Product is no longer available",
                    product != null ? product.imageUrl() : null,
                    unitPrice,
                    item.getQuantity(),
                    totalPrice,
                    available,
                    unavailableReason
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

        ProductInternalResponse product = getProduct(request.productId());

        for (CartItem item : cartItems) {
            if (item.getProductId().equals(request.productId())) {
                validateAvailableProduct(product, item.getQuantity() + request.quantity());
                item.setQuantity(item.getQuantity() + request.quantity());
                return;
            }
        }

        validateAvailableProduct(product, request.quantity());

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
    @Transactional
    public void removeCartItemsInternal(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() ->
                        new BusinessException(CartErrorMessage.CART_NOT_FOUND)
                );

        Set<Long> productIdSet = new HashSet<>(productIds);
        cart.getItems().removeIf(item -> productIdSet.contains(item.getProductId()));
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

    private String getUnavailableReason(ProductInternalResponse product, Integer quantity) {
        if (product == null) {
            return PRODUCT_NOT_FOUND;
        }

        if (!Boolean.TRUE.equals(product.active())) {
            return PRODUCT_INACTIVE;
        }

        if (product.stock() == null || product.stock() < quantity) {
            return INSUFFICIENT_STOCK;
        }

        return null;
    }

    private ProductInternalResponse getProduct(Long productId) {
        return productServiceClient.getProductsByIds(
                        Collections.singletonList(productId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(CartErrorMessage.PRODUCT_NOT_AVAILABLE));
    }

    private void validateAvailableProduct(ProductInternalResponse product, Integer requestedQuantity) {
        if (!Boolean.TRUE.equals(product.active())) {
            throw new BusinessException(CartErrorMessage.PRODUCT_NOT_AVAILABLE);
        }

        if (product.stock() == null || product.stock() < requestedQuantity) {
            throw new BusinessException(CartErrorMessage.PRODUCT_NOT_AVAILABLE);
        }
    }
}
