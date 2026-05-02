package com.demir.ecommerce.cartservice.dto;



import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice,
        Boolean available,
        String unavailableReason
) {}
