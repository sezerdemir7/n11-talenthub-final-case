package com.demir.ecommerce.userservice.integration.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.userservice.dto.address.request.AddressRequest;
import com.demir.ecommerce.userservice.dto.address.response.AddressInternalResponse;
import com.demir.ecommerce.userservice.dto.address.response.AddressResponse;
import com.demir.ecommerce.userservice.entity.Address;
import com.demir.ecommerce.userservice.entity.Role;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.repository.AddressRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.impl.AddressServiceImpl;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AddressServiceImpl Integration Tests")
class AddressServiceImplIntegrationTest {

    @Autowired
    private AddressServiceImpl addressService;

    @Autowired
    private AddressRepository addressRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserRepository userRepository;

    private static final Long USER_ID_PLACEHOLDER = 1L;

    @AfterEach
    void cleanup() {
        addressRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User savedUser() {
        return userRepository.save(
                new User("John", "Doe", "user@test.com", "pass", Set.of(Role.CUSTOMER), true)
        );
    }

    private AddressRequest request(String title, boolean isDefault) {
        return new AddressRequest(
                title,
                "Istanbul",
                "Kadikoy",
                "Test Address",
                "34700",
                isDefault
        );
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Should persist first address as default")
        void create_firstAddress_persistsAsDefault() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                AddressResponse response = addressService.create(request("Home", false));

                Address fromDb = addressRepository.findById(response.id()).orElseThrow();
                assertThat(fromDb.getTitle()).isEqualTo("Home");
                assertThat(fromDb.getIsDefault()).isTrue();
            }
        }

        @Test
        @DisplayName("Should clear old default when new default address is created")
        void create_newDefault_clearsOldDefault() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                addressService.create(request("Home", true));
                addressService.create(request("Work", true));

                List<Address> addresses = addressRepository.findAllByUserId(user.getId());
                assertThat(addresses).hasSize(2);
                assertThat(addresses).filteredOn(Address::getIsDefault).hasSize(1);
                assertThat(addresses.stream().filter(Address::getIsDefault).findFirst().orElseThrow().getTitle())
                        .isEqualTo("Work");
            }
        }
    }

    @Nested
    @DisplayName("getMyAddresses()")
    class GetMyAddresses {

        @Test
        @DisplayName("Should return addresses from database")
        void getMyAddresses_existingAddresses_returnsList() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                addressService.create(request("Home", false));

                List<AddressResponse> response = addressService.getMyAddresses();

                assertThat(response).hasSize(1);
                assertThat(response.get(0).title()).isEqualTo("Home");
            }
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Should update address in database")
        void update_existingAddress_updatesDatabase() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                AddressResponse created = addressService.create(request("Home", false));
                AddressResponse updated = addressService.update(created.id(), request("Updated Home", true));

                Address fromDb = addressRepository.findById(created.id()).orElseThrow();
                assertThat(updated.title()).isEqualTo("Updated Home");
                assertThat(fromDb.getTitle()).isEqualTo("Updated Home");
                assertThat(fromDb.getIsDefault()).isTrue();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when address does not belong to user")
        void update_addressNotFound_throwsException() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                assertThatThrownBy(() -> addressService.update(999L, request("Home", false)))
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Should delete address from database")
        void delete_existingAddress_deletesFromDatabase() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                AddressResponse created = addressService.create(request("Home", false));
                addressService.delete(created.id());

                assertThat(addressRepository.findById(created.id())).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("setDefault()")
    class SetDefault {

        @Test
        @DisplayName("Should set selected address as only default")
        void setDefault_existingAddress_setsOnlyDefault() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                addressService.create(request("Home", true));
                AddressResponse work = addressService.create(request("Work", false));

                addressService.setDefault(work.id());

                List<Address> addresses = addressRepository.findAllByUserId(user.getId());
                assertThat(addresses).filteredOn(Address::getIsDefault).hasSize(1);
                assertThat(addresses.stream().filter(Address::getIsDefault).findFirst().orElseThrow().getTitle())
                        .isEqualTo("Work");
            }
        }
    }

    @Nested
    @DisplayName("getInternalAddress()")
    class GetInternalAddress {

        @Test
        @DisplayName("Should return internal address from database")
        void getInternalAddress_existingAddress_returnsInternalResponse() {
            User user = savedUser();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(user.getId());

                AddressResponse created = addressService.create(request("Home", true));

                AddressInternalResponse response = addressService.getInternalAddress(user.getId(), created.id());

                assertThat(response.userId()).isEqualTo(user.getId());
                assertThat(response.title()).isEqualTo("Home");
                assertThat(response.isDefault()).isTrue();
            }
        }
    }
}
