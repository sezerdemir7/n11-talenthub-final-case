package com.demir.ecommerce.userservice.unit.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.dto.user.request.UpdateUserRequest;
import com.demir.ecommerce.userservice.dto.user.response.UserResponse;
import com.demir.ecommerce.userservice.entity.Role;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@test.com";

    private User user() {
        User user = new User("John", "Doe", EMAIL, "pass", Set.of(Role.CUSTOMER), true);
        user.setId(USER_ID);
        return user;
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        @Test
        @DisplayName("Should return current user by email")
        void getCurrentUser_existingEmail_returnsUser() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user()));

            UserResponse response = userService.getCurrentUser(EMAIL);

            assertThat(response.id()).isEqualTo(USER_ID);
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.roles()).contains("CUSTOMER");
            assertThat(response.active()).isTrue();
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not found")
        void getCurrentUser_userNotFound_throwsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getCurrentUser(EMAIL))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("Should return user by id")
        void getUserById_existingUser_returnsUser() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

            UserResponse response = userService.getUserById(USER_ID);

            assertThat(response.id()).isEqualTo(USER_ID);
            assertThat(response.firstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not found")
        void getUserById_userNotFound_throwsException() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(USER_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("existsById()")
    class ExistsById {

        @Test
        @DisplayName("Should return true when user exists")
        void existsById_existingUser_returnsTrue() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            boolean result = userService.existsById(USER_ID);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("updateCurrentUser()")
    class UpdateCurrentUser {

        @Test
        @DisplayName("Should update first name and last name")
        void updateCurrentUser_existingUser_updatesUser() {
            User user = user();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            UserResponse response = userService.updateCurrentUser(
                    EMAIL,
                    new UpdateUserRequest("Jane", "Smith")
            );

            assertThat(response.firstName()).isEqualTo("Jane");
            assertThat(response.lastName()).isEqualTo("Smith");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not found")
        void updateCurrentUser_userNotFound_throwsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.updateCurrentUser(EMAIL, new UpdateUserRequest("Jane", "Smith"))
            ).isInstanceOf(BusinessException.class);

            verify(userRepository, never()).save(any());
        }
    }
}
