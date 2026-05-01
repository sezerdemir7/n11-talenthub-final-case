package com.demir.ecommerce.userservice.service.impl;

import com.demir.ecommerce.commonlib.event.seller.SellerSuspendedEvent;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.dto.seller.request.SellerStatusUpdateRequest;
import com.demir.ecommerce.userservice.dto.seller.request.UpdateSellerProfileRequest;
import com.demir.ecommerce.userservice.dto.seller.response.SellerProfileResponse;
import com.demir.ecommerce.userservice.entity.Role;
import com.demir.ecommerce.userservice.entity.SellerProfile;
import com.demir.ecommerce.userservice.entity.SellerStatus;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.exception.message.UserErrorMessage;
import com.demir.ecommerce.userservice.messaging.SellerEventPublisher;
import com.demir.ecommerce.userservice.repository.SellerProfileRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.SellerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SellerServiceImpl implements SellerService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final SellerEventPublisher sellerEventPublisher;

    public SellerServiceImpl(SellerProfileRepository sellerProfileRepository,
                             UserRepository userRepository, SellerEventPublisher sellerEventPublisher) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.userRepository = userRepository;
        this.sellerEventPublisher = sellerEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProfileResponse getByUserId(Long userId) {
        SellerProfile sellerProfile = findSellerProfileByUserId(userId);
        return mapToResponse(sellerProfile);
    }

    @Override
    @Transactional
    public SellerProfileResponse updateMyProfile(Long userId, UpdateSellerProfileRequest request) {
        SellerProfile sellerProfile = findSellerProfileByUserId(userId);

        sellerProfile.setStoreName(request.storeName());
        sellerProfile.setCompanyName(request.companyName());
        sellerProfile.setStoreDescription(request.storeDescription());

        SellerProfile updatedProfile = sellerProfileRepository.save(sellerProfile);

        return mapToResponse(updatedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerProfileResponse> getPendingApplications() {
        return sellerProfileRepository.findByStatus(SellerStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public SellerProfileResponse updateStatus(Long sellerProfileId, SellerStatusUpdateRequest request) {

        SellerProfile sellerProfile = sellerProfileRepository.findById(sellerProfileId)
                .orElseThrow(() -> new BusinessException(UserErrorMessage.SELLER_PROFILE_NOT_FOUND));

        User user = sellerProfile.getUser();

        if (request.status() == SellerStatus.REJECTED
                && sellerProfile.getStatus() != SellerStatus.PENDING) {
            throw new BusinessException(UserErrorMessage.INVALID_SELLER_STATUS);
        }

        if (request.status() == SellerStatus.APPROVED) {
            sellerProfile.setStatus(SellerStatus.APPROVED);
            sellerProfile.setIsVerified(true);
            user.addRole(Role.SELLER);
        }

        if (request.status() == SellerStatus.REJECTED) {
            sellerProfile.setStatus(SellerStatus.REJECTED);
            sellerProfile.setIsVerified(false);
            user.removeRole(Role.SELLER);
        }

        if (request.status() == SellerStatus.SUSPENDED) {
            sellerProfile.setStatus(SellerStatus.SUSPENDED);
            sellerProfile.setIsVerified(false);
            user.removeRole(Role.SELLER);

            sellerEventPublisher.publishSellerSuspended(
                    new SellerSuspendedEvent(user.getId())
            );
        }

        userRepository.save(user);
        SellerProfile savedProfile = sellerProfileRepository.save(sellerProfile);

        return mapToResponse(savedProfile);
    }


    private SellerProfile findSellerProfileByUserId(Long userId) {
        return sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(UserErrorMessage.SELLER_PROFILE_NOT_FOUND));
    }

    private SellerProfileResponse mapToResponse(SellerProfile sellerProfile) {
        return new SellerProfileResponse(
                sellerProfile.getId(),
                sellerProfile.getUser().getId(),
                sellerProfile.getStoreName(),
                sellerProfile.getCompanyName(),
                sellerProfile.getTaxNumber(),
                sellerProfile.getStoreDescription(),
                sellerProfile.getStatus(),
                Boolean.TRUE.equals(sellerProfile.getIsVerified())
        );
    }
}
