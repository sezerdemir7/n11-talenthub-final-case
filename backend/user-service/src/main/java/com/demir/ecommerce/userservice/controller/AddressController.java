package com.demir.ecommerce.userservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.userservice.dto.address.request.AddressRequest;
import com.demir.ecommerce.userservice.dto.address.response.AddressResponse;
import com.demir.ecommerce.userservice.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<RestResponse<AddressResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.create(userId, request);
        return ResponseEntity.ok(RestResponse.of(response, "Address created successfully"));
    }

    @GetMapping
    public ResponseEntity<RestResponse<List<AddressResponse>>> getMyAddresses(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<AddressResponse> response = addressService.getMyAddresses(userId);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<RestResponse<AddressResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.update(userId, addressId, request);
        return ResponseEntity.ok(RestResponse.of(response, "Address updated successfully"));
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<RestResponse<AddressResponse>> setDefault(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId
    ) {
        AddressResponse response = addressService.setDefault(userId, addressId);
        return ResponseEntity.ok(RestResponse.of(response, "Default address updated successfully"));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<RestResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId
    ) {
        addressService.delete(userId, addressId);
        return ResponseEntity.ok(RestResponse.success("Address deleted successfully"));
    }
}
