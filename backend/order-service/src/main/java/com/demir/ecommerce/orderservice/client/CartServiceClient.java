package com.demir.ecommerce.orderservice.client;



import com.demir.ecommerce.orderservice.dto.cart.CartInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service", path = "/internal/carts")
public interface CartServiceClient {

    @GetMapping
    CartInternalResponse getCart(
            @RequestHeader("X-User-Id") Long userId
    );

    @DeleteMapping("/clear")
    void clearCart(
            @RequestHeader("X-User-Id") Long userId
    );
}