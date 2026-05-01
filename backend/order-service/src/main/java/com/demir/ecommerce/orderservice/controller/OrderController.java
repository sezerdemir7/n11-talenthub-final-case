package com.demir.ecommerce.orderservice.controller;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.orderservice.dto.CheckoutRequest;
import com.demir.ecommerce.orderservice.dto.OrderItemResponse;
import com.demir.ecommerce.orderservice.dto.OrderResponse;
import com.demir.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<RestResponse<OrderResponse>> getOrderById(
            @PathVariable Long orderId
    ) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(RestResponse.of(response));
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<RestResponse<PageResponse<OrderResponse>>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<OrderResponse> response =
                orderService.getOrdersByUserId(userId, page, size);

        return ResponseEntity.ok(RestResponse.of(response));
    }


    @GetMapping("/{orderId}/items")
    public ResponseEntity<RestResponse<PageResponse<OrderItemResponse>>> getOrderItems(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<OrderItemResponse> response =
                orderService.getOrderItems(orderId, page, size);

        return ResponseEntity.ok(RestResponse.of(response));
    }



    @DeleteMapping("/{orderId}")
    public ResponseEntity<RestResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(RestResponse.of(null, "Order cancelled successfully"));
    }


    @PostMapping("/checkout")
    public ResponseEntity<RestResponse<OrderResponse>> checkout(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CheckoutRequest request
    ) {
        OrderResponse response = orderService.checkout(userId, request);
        return ResponseEntity.ok(RestResponse.of(response, "Order created successfully"));
    }

}