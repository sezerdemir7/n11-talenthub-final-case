package com.demir.ecommerce.cartservice.dto.product;

import java.math.BigDecimal;

public record ProductInternalResponse(
        Long id,
        String name,
        String imageUrl,
        BigDecimal price,
        Integer stock,
        Boolean active
) {}