package com.demir.ecommerce.userservice.service;

import com.demir.ecommerce.userservice.dto.seller.request.SellerFilterRequest;
import com.demir.ecommerce.userservice.dto.seller.request.SellerStatusUpdateRequest;
import com.demir.ecommerce.userservice.dto.seller.request.UpdateSellerProfileRequest;
import com.demir.ecommerce.userservice.dto.seller.response.SellerProfileResponse;
import com.demir.ecommerce.userservice.dto.seller.response.SellerResponse;
import com.demir.ecommerce.commonlib.dto.PageResponse;


import java.util.List;

public interface SellerService {

    SellerProfileResponse getMyProfile();

    SellerProfileResponse updateMyProfile(UpdateSellerProfileRequest request);

    List<SellerProfileResponse> getPendingApplications();

    SellerProfileResponse updateStatus(Long sellerProfileId, SellerStatusUpdateRequest request);

    PageResponse<SellerResponse> getAllSellers(SellerFilterRequest filter, int page, int size);

}