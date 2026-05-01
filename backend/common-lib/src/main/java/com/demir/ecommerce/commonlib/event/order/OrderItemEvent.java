package com.demir.ecommerce.commonlib.event.order;

import java.math.BigDecimal;

public record OrderItemEvent(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
) {
}
