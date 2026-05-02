package com.demir.ecommerce.userservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.userservice.dto.seller.request.SellerFilterRequest;
import com.demir.ecommerce.userservice.dto.seller.request.SellerStatusUpdateRequest;
import com.demir.ecommerce.userservice.dto.seller.request.UpdateSellerProfileRequest;
import com.demir.ecommerce.userservice.dto.seller.response.SellerProfileResponse;
import com.demir.ecommerce.userservice.dto.seller.response.SellerResponse;
import com.demir.ecommerce.userservice.entity.SellerStatus;
import com.demir.ecommerce.userservice.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.demir.ecommerce.commonlib.dto.PageResponse;

import java.util.List;

@Tag(name = "Seller", description = "Seller management operations")
@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Operation(summary = "Get my seller profile", description = "Returns authenticated seller's profile")
    @GetMapping("/me")
    public ResponseEntity<RestResponse<SellerProfileResponse>> getMySellerProfile() {
        SellerProfileResponse response = sellerService.getMyProfile();
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Get all sellers", description = "Returns paginated sellers with optional filters — admin only")
    @GetMapping
    public ResponseEntity<RestResponse<PageResponse<SellerResponse>>> getAllSellers(
            @RequestParam(required = false) String storeName,
            @RequestParam(required = false) SellerStatus status,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        SellerFilterRequest filter = new SellerFilterRequest(storeName, status, verified);
        PageResponse<SellerResponse> response = sellerService.getAllSellers(filter, page, size);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Update my seller profile", description = "Updates authenticated seller's profile")
    @PutMapping("/me")
    public ResponseEntity<RestResponse<SellerProfileResponse>> updateMySellerProfile(
            @Valid @RequestBody UpdateSellerProfileRequest request
    ) {
        SellerProfileResponse response = sellerService.updateMyProfile(request);
        return ResponseEntity.ok(RestResponse.of(response, "Seller profile updated successfully"));
    }

    @Operation(summary = "Get pending applications", description = "Returns all pending seller applications — admin only")
    @GetMapping("/applications/pending")
    public ResponseEntity<RestResponse<List<SellerProfileResponse>>> getPendingApplications() {
        List<SellerProfileResponse> response = sellerService.getPendingApplications();
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @Operation(summary = "Update seller status", description = "Updates seller application status — admin only")
    @PatchMapping("/{sellerProfileId}/status")
    public ResponseEntity<RestResponse<SellerProfileResponse>> updateStatus(
            @PathVariable Long sellerProfileId,
            @Valid @RequestBody SellerStatusUpdateRequest request
    ) {
        SellerProfileResponse response = sellerService.updateStatus(sellerProfileId, request);
        return ResponseEntity.ok(RestResponse.of(response, "Seller application status updated successfully"));
    }
}