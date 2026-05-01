package com.demir.ecommerce.cartservice.controller;

import com.demir.ecommerce.cartservice.dto.AddToCartRequest;
import com.demir.ecommerce.cartservice.dto.CartResponse;
import com.demir.ecommerce.cartservice.service.CartService;
import com.demir.ecommerce.commonlib.dto.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Cart management operations")
@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Get cart", description = "Returns user cart with items")
    @GetMapping
    public ResponseEntity<RestResponse<CartResponse>> getCart(
            @RequestHeader("X-User-Id") Long userId
    ) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Add product to cart", description = "Adds product to user cart")
    @PostMapping
    public ResponseEntity<RestResponse<Void>> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddToCartRequest request
    ) {
        cartService.addToCart(userId, request);
        return ResponseEntity.ok(RestResponse.of(null, "Product added to cart"));
    }

    @Operation(summary = "Clear cart", description = "Removes all items from cart")
    @DeleteMapping
    public ResponseEntity<RestResponse<Void>> clearCart(
            @RequestHeader("X-User-Id") Long userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(RestResponse.of(null, "Cart cleared"));
    }
}