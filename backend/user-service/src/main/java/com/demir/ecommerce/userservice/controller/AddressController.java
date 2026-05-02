package com.demir.ecommerce.userservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.userservice.dto.address.request.AddressRequest;
import com.demir.ecommerce.userservice.dto.address.response.AddressResponse;
import com.demir.ecommerce.userservice.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Address", description = "Address management operations")
@RestController
@RequestMapping("/api/v1/users/me/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "Create address", description = "Creates a new address for the authenticated user")
    @PostMapping
    public ResponseEntity<RestResponse<AddressResponse>> create(
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.create(request);
        return ResponseEntity.ok(RestResponse.of(response, "Address created successfully"));
    }

    @Operation(summary = "Get my addresses", description = "Returns all addresses of the authenticated user")
    @GetMapping
    public ResponseEntity<RestResponse<List<AddressResponse>>> getMyAddresses() {
        List<AddressResponse> response = addressService.getMyAddresses();
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Update address", description = "Updates an address of the authenticated user")
    @PutMapping("/{addressId}")
    public ResponseEntity<RestResponse<AddressResponse>> update(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.update(addressId, request);
        return ResponseEntity.ok(RestResponse.of(response, "Address updated successfully"));
    }

    @Operation(summary = "Set default address", description = "Sets an address as default for the authenticated user")
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<RestResponse<AddressResponse>> setDefault(
            @PathVariable Long addressId
    ) {
        AddressResponse response = addressService.setDefault(addressId);
        return ResponseEntity.ok(RestResponse.of(response, "Default address updated successfully"));
    }

    @Operation(summary = "Delete address", description = "Deletes an address of the authenticated user")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<RestResponse<Void>> delete(@PathVariable Long addressId) {
        addressService.delete(addressId);
        return ResponseEntity.ok(RestResponse.success("Address deleted successfully"));
    }
}