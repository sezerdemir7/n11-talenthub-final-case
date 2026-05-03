package com.demir.ecommerce.userservice.unit.service.impl;

import com.demir.ecommerce.commonlib.dto.PageResponse;
import com.demir.ecommerce.commonlib.event.seller.SellerActivatedEvent;
import com.demir.ecommerce.commonlib.event.seller.SellerSuspendedEvent;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.userservice.dto.seller.request.SellerFilterRequest;
import com.demir.ecommerce.userservice.dto.seller.request.SellerStatusUpdateRequest;
import com.demir.ecommerce.userservice.dto.seller.request.UpdateSellerProfileRequest;
import com.demir.ecommerce.userservice.dto.seller.response.SellerProfileResponse;
import com.demir.ecommerce.userservice.dto.seller.response.SellerResponse;
import com.demir.ecommerce.userservice.entity.Role;
import com.demir.ecommerce.userservice.entity.SellerProfile;
import com.demir.ecommerce.userservice.entity.SellerStatus;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.messaging.SellerEventPublisher;
import com.demir.ecommerce.userservice.repository.SellerProfileRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.impl.SellerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerServiceImpl Unit Tests")
class SellerServiceImplTest {

    @Mock
    private SellerProfileRepository sellerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SellerEventPublisher sellerEventPublisher;

    @InjectMocks
    private SellerServiceImpl sellerService;

    private static final Long USER_ID = 1L;
    private static final Long SELLER_PROFILE_ID = 10L;

    private User user(Set<Role> roles) {
        User user = new User("John", "Doe", "seller@test.com", "pass", new HashSet<>(roles), true);
        user.setId(USER_ID);
        return user;
    }

    private SellerProfile sellerProfile(SellerStatus status, boolean verified) {
        SellerProfile profile = new SellerProfile(
                user(Set.of(Role.CUSTOMER)),
                "Store",
                "Company",
                "1234567890",
                "Description",
                status,
                verified
        );
        profile.setId(SELLER_PROFILE_ID);
        return profile;
    }

    @Nested
    @DisplayName("getMyProfile()")
    class GetMyProfile {

        @Test
        @DisplayName("Should return current user's seller profile")
        void getMyProfile_existingProfile_returnsProfile() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(sellerProfileRepository.findByUserId(USER_ID))
                        .thenReturn(Optional.of(sellerProfile(SellerStatus.APPROVED, true)));

                SellerProfileResponse response = sellerService.getMyProfile();

                assertThat(response.id()).isEqualTo(SELLER_PROFILE_ID);
                assertThat(response.userId()).isEqualTo(USER_ID);
                assertThat(response.status()).isEqualTo(SellerStatus.APPROVED);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when profile does not exist")
        void getMyProfile_missingProfile_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(sellerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> sellerService.getMyProfile())
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("updateMyProfile()")
    class UpdateMyProfile {

        @Test
        @DisplayName("Should update current seller profile")
        void updateMyProfile_existingProfile_updatesProfile() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                SellerProfile profile = sellerProfile(SellerStatus.APPROVED, true);
                when(sellerProfileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
                when(sellerProfileRepository.save(profile)).thenReturn(profile);

                SellerProfileResponse response = sellerService.updateMyProfile(
                        new UpdateSellerProfileRequest("New Store", "New Company", "New Description")
                );

                assertThat(response.storeName()).isEqualTo("New Store");
                assertThat(response.companyName()).isEqualTo("New Company");
                assertThat(profile.getStoreDescription()).isEqualTo("New Description");
            }
        }
    }

    @Nested
    @DisplayName("getPendingApplications()")
    class GetPendingApplications {

        @Test
        @DisplayName("Should return pending applications for admin")
        void getPendingApplications_admin_returnsPendingProfiles() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                when(sellerProfileRepository.findByStatus(SellerStatus.PENDING))
                        .thenReturn(List.of(sellerProfile(SellerStatus.PENDING, false)));

                List<SellerProfileResponse> response = sellerService.getPendingApplications();

                assertThat(response).hasSize(1);
                assertThat(response.get(0).status()).isEqualTo(SellerStatus.PENDING);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not admin")
        void getPendingApplications_notAdmin_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(false);

                assertThatThrownBy(() -> sellerService.getPendingApplications())
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Should approve seller and publish activation event")
        void updateStatus_approved_addsSellerRoleAndPublishesEvent() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                SellerProfile profile = sellerProfile(SellerStatus.PENDING, false);
                when(sellerProfileRepository.findById(SELLER_PROFILE_ID)).thenReturn(Optional.of(profile));
                when(sellerProfileRepository.save(profile)).thenReturn(profile);

                SellerProfileResponse response = sellerService.updateStatus(
                        SELLER_PROFILE_ID,
                        new SellerStatusUpdateRequest(SellerStatus.APPROVED)
                );

                assertThat(response.status()).isEqualTo(SellerStatus.APPROVED);
                assertThat(response.verified()).isTrue();
                assertThat(profile.getUser().getRoles()).contains(Role.SELLER);
                verify(userRepository).save(profile.getUser());
                verify(sellerEventPublisher).publishSellerActivated(any(SellerActivatedEvent.class));
            }
        }

        @Test
        @DisplayName("Should suspend seller and publish suspension event")
        void updateStatus_suspended_removesSellerRoleAndPublishesEvent() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                SellerProfile profile = sellerProfile(SellerStatus.APPROVED, true);
                profile.getUser().addRole(Role.SELLER);
                when(sellerProfileRepository.findById(SELLER_PROFILE_ID)).thenReturn(Optional.of(profile));
                when(sellerProfileRepository.save(profile)).thenReturn(profile);

                SellerProfileResponse response = sellerService.updateStatus(
                        SELLER_PROFILE_ID,
                        new SellerStatusUpdateRequest(SellerStatus.SUSPENDED)
                );

                assertThat(response.status()).isEqualTo(SellerStatus.SUSPENDED);
                assertThat(profile.getUser().getRoles()).doesNotContain(Role.SELLER);
                verify(sellerEventPublisher).publishSellerSuspended(any(SellerSuspendedEvent.class));
            }
        }

        @Test
        @DisplayName("Should reject only pending applications")
        void updateStatus_rejectedNonPending_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                when(sellerProfileRepository.findById(SELLER_PROFILE_ID))
                        .thenReturn(Optional.of(sellerProfile(SellerStatus.APPROVED, true)));

                assertThatThrownBy(() -> sellerService.updateStatus(
                        SELLER_PROFILE_ID,
                        new SellerStatusUpdateRequest(SellerStatus.REJECTED)
                )).isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("getAllSellers()")
    class GetAllSellers {

        @Test
        @DisplayName("Should return paginated seller responses for admin")
        void getAllSellers_admin_returnsPageResponse() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                when(sellerProfileRepository.findAll(any(Specification.class), any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(sellerProfile(SellerStatus.APPROVED, true))));

                PageResponse<SellerResponse> response = sellerService.getAllSellers(
                        new SellerFilterRequest(null, null, null),
                        0,
                        10
                );

                assertThat(response.getContent()).hasSize(1);
                assertThat(response.getContent().get(0).storeName()).isEqualTo("Store");
            }
        }
    }
}
