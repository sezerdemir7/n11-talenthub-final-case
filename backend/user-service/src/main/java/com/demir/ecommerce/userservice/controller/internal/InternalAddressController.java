package com.demir.ecommerce.userservice.controller.internal;

import com.demir.ecommerce.userservice.dto.address.response.AddressInternalResponse;
import com.demir.ecommerce.userservice.service.AddressService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users/{userId}/addresses")
public class InternalAddressController {

    private final AddressService addressService;

    public InternalAddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/{addressId}")
    public AddressInternalResponse getAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId
    ) {
        return addressService.getInternalAddress(userId, addressId);
    }
}
