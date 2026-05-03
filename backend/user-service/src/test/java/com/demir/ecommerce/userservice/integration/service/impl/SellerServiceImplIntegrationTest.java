package com.demir.ecommerce.userservice.integration.service.impl;

import com.demir.ecommerce.commonlib.dto.PageResponse;
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
import com.demir.ecommerce.userservice.repository.RefreshTokenRepository;
import com.demir.ecommerce.userservice.repository.SellerProfileRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.SellerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SellerServiceImpl Integration Tests")
class SellerServiceImplIntegrationTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @AfterEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User savedUser(Set<Role> roles) {
        return userRepository.save(
                new User("John", "Doe", "seller@test.com", "pass", roles, true)
        );
    }

    private SellerProfile savedSellerProfile(User user, SellerStatus status, boolean verified) {
        return sellerProfileRepository.save(
                new SellerProfile(
                        user,
                        "Store",
                        "Company",
                        "1234567890",
                        "Description",
                        status,
                        verified
                )
        );
    }

    @Nested
    @DisplayName("getMyProfile()")
    class GetMyProfile {

        @Test
        @DisplayName("Should return seller profile for current user")
        void getMyProfile_existingProfile_returnsProfile() {
            User user = savedUser(Set.of(Role.CUSTOMER, Role.SELLER));
            savedSellerProfile(user, SellerStatus.APPROVED, true);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                SellerProfileResponse response = sellerService.getMyProfile();

                assertThat(response.userId()).isEqualTo(user.getId());
                assertThat(response.storeName()).isEqualTo("Store");
                assertThat(response.verified()).isTrue();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when profile is missing")
        void getMyProfile_missingProfile_throwsException() {
            User user = savedUser(Set.of(Role.CUSTOMER));

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                assertThatThrownBy(() -> sellerService.getMyProfile())
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("updateMyProfile()")
    class UpdateMyProfile {

        @Test
        @DisplayName("Should update seller profile in database")
        void updateMyProfile_existingProfile_updatesDatabase() {
            User user = savedUser(Set.of(Role.CUSTOMER, Role.SELLER));
            SellerProfile profile = savedSellerProfile(user, SellerStatus.APPROVED, true);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                SellerProfileResponse response = sellerService.updateMyProfile(
                        new UpdateSellerProfileRequest("New Store", "New Company", "New Description")
                );

                SellerProfile fromDb = sellerProfileRepository.findById(profile.getId()).orElseThrow();
                assertThat(response.storeName()).isEqualTo("New Store");
                assertThat(fromDb.getCompanyName()).isEqualTo("New Company");
                assertThat(fromDb.getStoreDescription()).isEqualTo("New Description");
            }
        }
    }

    @Nested
    @DisplayName("getPendingApplications()")
    class GetPendingApplications {

        @Test
        @DisplayName("Should return pending applications for admin")
        void getPendingApplications_admin_returnsPendingProfiles() {
            User user = savedUser(Set.of(Role.CUSTOMER));
            savedSellerProfile(user, SellerStatus.PENDING, false);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                List<SellerProfileResponse> response = sellerService.getPendingApplications();

                assertThat(response).hasSize(1);
                assertThat(response.get(0).status()).isEqualTo(SellerStatus.PENDING);
            }
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Should approve seller profile and add seller role")
        void updateStatus_approved_updatesDatabase() {
            User user = savedUser(Set.of(Role.CUSTOMER));
            SellerProfile profile = savedSellerProfile(user, SellerStatus.PENDING, false);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                SellerProfileResponse response = sellerService.updateStatus(
                        profile.getId(),
                        new SellerStatusUpdateRequest(SellerStatus.APPROVED)
                );

                User fromDb = userRepository.findById(user.getId()).orElseThrow();
                assertThat(response.status()).isEqualTo(SellerStatus.APPROVED);
                assertThat(response.verified()).isTrue();
                assertThat(fromDb.getRoles()).contains(Role.SELLER);
            }
        }

        @Test
        @DisplayName("Should suspend seller profile and remove seller role")
        void updateStatus_suspended_updatesDatabase() {
            User user = savedUser(Set.of(Role.CUSTOMER, Role.SELLER));
            SellerProfile profile = savedSellerProfile(user, SellerStatus.APPROVED, true);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                SellerProfileResponse response = sellerService.updateStatus(
                        profile.getId(),
                        new SellerStatusUpdateRequest(SellerStatus.SUSPENDED)
                );

                User fromDb = userRepository.findById(user.getId()).orElseThrow();
                assertThat(response.status()).isEqualTo(SellerStatus.SUSPENDED);
                assertThat(response.verified()).isFalse();
                assertThat(fromDb.getRoles()).doesNotContain(Role.SELLER);
            }
        }
    }

    @Nested
    @DisplayName("getAllSellers()")
    class GetAllSellers {

        @Test
        @DisplayName("Should return filtered seller page for admin")
        void getAllSellers_admin_returnsPage() {
            User user = savedUser(Set.of(Role.CUSTOMER, Role.SELLER));
            savedSellerProfile(user, SellerStatus.APPROVED, true);

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                PageResponse<SellerResponse> response = sellerService.getAllSellers(
                        new SellerFilterRequest("Sto", SellerStatus.APPROVED, true),
                        0,
                        10
                );

                assertThat(response.getContent()).hasSize(1);
                assertThat(response.getContent().get(0).storeName()).isEqualTo("Store");
                assertThat(response.getTotalElements()).isEqualTo(1);
            }
        }
    }
}
