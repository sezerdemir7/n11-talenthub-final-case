package com.demir.ecommerce.userservice.unit.service.impl;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressServiceImpl Unit Tests")
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;


    private static final Long USER_ID = 1L;
    private static final Long ADDRESS_ID = 10L;

    private User user() {
        User user = new User("John", "Doe", "user@test.com", "pass", Set.of(Role.CUSTOMER), true);
        user.setId(USER_ID);
        return user;
    }

    private Address address(boolean isDefault) {
        Address address = new Address();
        address.setId(ADDRESS_ID);
        address.setUser(user());
        address.setTitle("Home");
        address.setCity("Istanbul");
        address.setDistrict("Kadikoy");
        address.setFullAddress("Test Address");
        address.setPostalCode("34700");
        address.setIsDefault(isDefault);
        return address;
    }

    private AddressRequest request(boolean isDefault) {
        return new AddressRequest(
                "Home",
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
        @DisplayName("Should create address as default when user has no default address")
        void create_noDefaultAddress_createsDefaultAddress() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
                when(addressRepository.findByUserIdAndIsDefaultTrue(USER_ID)).thenReturn(Optional.empty());
                when(addressRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
                when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
                    Address a = invocation.getArgument(0);
                    a.setId(ADDRESS_ID);
                    return a;
                });

                AddressResponse response = addressService.create(request(false));

                assertThat(response.id()).isEqualTo(ADDRESS_ID);
                assertThat(response.isDefault()).isTrue();
                verify(addressRepository).save(any(Address.class));
            }
        }

        @Test
        @DisplayName("Should create address as default when request default is true")
        void create_requestDefaultTrue_createsDefaultAddress() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
                when(addressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(address(true)));
                when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AddressResponse response = addressService.create(request(true));

                assertThat(response.isDefault()).isTrue();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not found")
        void create_userNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> addressService.create(request(false)))
                        .isInstanceOf(BusinessException.class);

                verify(addressRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("getMyAddresses()")
    class GetMyAddresses {

        @Test
        @DisplayName("Should return current user's addresses")
        void getMyAddresses_existingAddresses_returnsList() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(addressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(address(true)));

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
        @DisplayName("Should update address when it belongs to current user")
        void update_existingAddress_updatesAddress() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                Address address = address(false);

                when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));
                when(addressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(address));
                when(addressRepository.save(address)).thenReturn(address);

                AddressResponse response = addressService.update(ADDRESS_ID, request(true));

                assertThat(response.isDefault()).isTrue();
                assertThat(address.getCity()).isEqualTo("Istanbul");
                verify(addressRepository, times(2)).save(address);
            }
        }


        @Test
        @DisplayName("Should throw BusinessException when address is not found")
        void update_addressNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> addressService.update(ADDRESS_ID, request(false)))
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Should delete address when it belongs to current user")
        void delete_existingAddress_deletesAddress() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                Address address = address(false);

                when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address));

                addressService.delete(ADDRESS_ID);

                verify(addressRepository).delete(address);
            }
        }
    }

    @Nested
    @DisplayName("setDefault()")
    class SetDefault {

        @Test
        @DisplayName("Should clear old default and set selected address as default")
        void setDefault_existingAddress_setsDefault() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::getUserId).thenReturn(USER_ID);
                Address oldDefault = address(true);
                Address selected = address(false);

                when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(selected));
                when(addressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(oldDefault, selected));
                when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

                AddressResponse response = addressService.setDefault(ADDRESS_ID);

                assertThat(response.isDefault()).isTrue();
                assertThat(oldDefault.getIsDefault()).isFalse();
                verify(addressRepository, times(3)).save(any(Address.class));
            }
        }

    }

    @Nested
    @DisplayName("getInternalAddress()")
    class GetInternalAddress {

        @Test
        @DisplayName("Should return internal address response")
        void getInternalAddress_existingAddress_returnsInternalResponse() {
            when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(Optional.of(address(true)));

            AddressInternalResponse response = addressService.getInternalAddress(USER_ID, ADDRESS_ID);

            assertThat(response.id()).isEqualTo(ADDRESS_ID);
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.isDefault()).isTrue();
        }
    }
}
