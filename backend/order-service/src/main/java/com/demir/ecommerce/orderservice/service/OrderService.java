package com.demir.ecommerce.orderservice.service;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.orderservice.dto.CheckoutRequest;
import com.demir.ecommerce.orderservice.dto.CreateOrderRequest;
import com.demir.ecommerce.orderservice.dto.OrderItemResponse;
import com.demir.ecommerce.orderservice.dto.OrderResponse;

public interface OrderService {

    OrderResponse getOrderById(Long orderId);

    PageResponse<OrderResponse> getOrdersByUserId(Long userId, int page, int size);

    PageResponse<OrderItemResponse> getOrderItems(Long orderId, int page, int size);

    void cancelOrder(Long orderId, Long userId);

    OrderResponse checkout(Long userId, CheckoutRequest request);


}