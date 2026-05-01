package com.demir.ecommerce.userservice.service;

import com.demir.ecommerce.userservice.dto.seller.request.SellerStatusUpdateRequest;
import com.demir.ecommerce.userservice.dto.seller.request.UpdateSellerProfileRequest;
import com.demir.ecommerce.userservice.dto.seller.response.SellerProfileResponse;

import java.util.List;

public interface SellerService {

    SellerProfileResponse getByUserId(Long userId);

    SellerProfileResponse updateMyProfile(Long userId, UpdateSellerProfileRequest request);

    List<SellerProfileResponse> getPendingApplications();

    SellerProfileResponse updateStatus(Long sellerProfileId, SellerStatusUpdateRequest request);
}
