package com.demir.ecommerce.userservice.controller;

import com.demir.ecommerce.commonlib.dto.RestResponse;
import com.demir.ecommerce.userservice.dto.seller.request.SellerStatusUpdateRequest;
import com.demir.ecommerce.userservice.dto.seller.request.UpdateSellerProfileRequest;
import com.demir.ecommerce.userservice.dto.seller.response.SellerProfileResponse;
import com.demir.ecommerce.userservice.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping("/me")
    public ResponseEntity<RestResponse<SellerProfileResponse>> getMySellerProfile(
            @RequestHeader("X-User-Id") Long userId
    ) {
        SellerProfileResponse response = sellerService.getByUserId(userId);
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @PutMapping("/me")
    public ResponseEntity<RestResponse<SellerProfileResponse>> updateMySellerProfile(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateSellerProfileRequest request
    ) {
        SellerProfileResponse response = sellerService.updateMyProfile(userId, request);
        return ResponseEntity.ok(RestResponse.of(response, "Seller profile updated successfully"));
    }

    @GetMapping("/applications/pending")
    public ResponseEntity<RestResponse<List<SellerProfileResponse>>> getPendingApplications() {
        List<SellerProfileResponse> response = sellerService.getPendingApplications();
        return ResponseEntity.ok(RestResponse.of(response));
    }

    @PatchMapping("/{sellerProfileId}/status")
    public ResponseEntity<RestResponse<SellerProfileResponse>> updateStatus(
            @PathVariable Long sellerProfileId,
            @Valid @RequestBody SellerStatusUpdateRequest request
    ) {
        SellerProfileResponse response = sellerService.updateStatus(sellerProfileId, request);
        return ResponseEntity.ok(RestResponse.of(response, "Seller application status updated successfully"));
    }
}
