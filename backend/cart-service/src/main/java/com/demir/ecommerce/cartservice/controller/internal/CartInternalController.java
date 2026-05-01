package com.demir.ecommerce.cartservice.controller.internal;


import com.demir.ecommerce.cartservice.dto.internal.CartInternalResponse;
import com.demir.ecommerce.cartservice.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/carts")
public class CartInternalController {

    private final CartService cartService;

    public CartInternalController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartInternalResponse getCartInternal(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return cartService.getInternalCart(userId);
    }

    @DeleteMapping("/clear")
    public void clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
    }
}