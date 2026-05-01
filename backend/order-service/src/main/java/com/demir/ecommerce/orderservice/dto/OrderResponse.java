package com.demir.ecommerce.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(

        Long orderId,
        Long userId,
        BigDecimal totalPrice,
        String status,
        AddressDto address,
        List<OrderItemResponse> items
) {}