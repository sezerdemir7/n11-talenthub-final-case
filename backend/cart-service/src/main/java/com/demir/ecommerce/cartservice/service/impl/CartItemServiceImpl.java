package com.demir.ecommerce.cartservice.service.impl;

import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.entity.Cart;
import com.demir.ecommerce.cartservice.entity.CartItem;
import com.demir.ecommerce.cartservice.exception.message.CartErrorMessage;
import com.demir.ecommerce.cartservice.repository.CartItemRepository;
import com.demir.ecommerce.cartservice.repository.CartRepository;
import com.demir.ecommerce.cartservice.service.CartItemService;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    public CartItemServiceImpl(CartItemRepository cartItemRepository,
                               CartRepository cartRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public CartItem addItem(Long cartId, AddToCartRequest request) {

        if (request.quantity() <= 0) {
            throw new BusinessException(CartErrorMessage.INVALID_QUANTITY);
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(
                        CartErrorMessage.CART_NOT_FOUND
                ));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProductId(request.productId());
        item.setQuantity(request.quantity());

        return cartItemRepository.save(item);
    }

    @Override
    public void removeItem(Long cartId, Long productId) {

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new BusinessException(
                        CartErrorMessage.CART_ITEM_NOT_FOUND
                ));

        cartItemRepository.delete(item);
    }

    @Override
    public CartItem updateQuantity(Long cartId, Long productId, Integer quantity) {

        if (quantity <= 0) {
            throw new BusinessException(CartErrorMessage.INVALID_QUANTITY);
        }

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new BusinessException(
                        CartErrorMessage.CART_ITEM_NOT_FOUND
                ));

        item.setQuantity(quantity);

        return cartItemRepository.save(item);
    }

    @Override
    public List<CartItem> getItemsByCartId(Long cartId) {
        return cartItemRepository.findAllByCartId(cartId);
    }

    @Override
    public void deleteAllByCartId(Long cartId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new BusinessException(
                        CartErrorMessage.CART_NOT_FOUND
                ));

        cart.getItems().clear();
        cartRepository.save(cart);
    }
}