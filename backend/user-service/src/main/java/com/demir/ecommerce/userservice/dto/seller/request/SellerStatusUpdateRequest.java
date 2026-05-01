package com.demir.ecommerce.userservice.dto.seller.request;

import com.demir.ecommerce.userservice.entity.SellerStatus;
import jakarta.validation.constraints.NotNull;

public record SellerStatusUpdateRequest(
        @NotNull
        SellerStatus status
) {
}
