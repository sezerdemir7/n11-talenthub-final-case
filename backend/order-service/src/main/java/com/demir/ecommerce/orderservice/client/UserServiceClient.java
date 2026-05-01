package com.demir.ecommerce.orderservice.client;

import com.demir.ecommerce.orderservice.config.FeignConfig;
import com.demir.ecommerce.orderservice.dto.user.AddressInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service",configuration = FeignConfig.class)
public interface UserServiceClient {

    @GetMapping("/internal/users/{userId}/addresses/{addressId}")
    AddressInternalResponse getAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    );
}
