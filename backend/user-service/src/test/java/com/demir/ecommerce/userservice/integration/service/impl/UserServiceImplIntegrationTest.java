package com.demir.ecommerce.userservice.integration.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.dto.user.request.UpdateUserRequest;
import com.demir.ecommerce.userservice.dto.user.response.UserResponse;
import com.demir.ecommerce.userservice.entity.Role;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserServiceImpl Integration Tests")
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;



    @Autowired
    private UserRepository userRepository;

    private static final String EMAIL = "user@test.com";

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    private User savedUser() {
        return userRepository.save(
                new User("John", "Doe", EMAIL, "pass", Set.of(Role.CUSTOMER), true)
        );
    }

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        @Test
        @DisplayName("Should return current user from database")
        void getCurrentUser_existingEmail_returnsUserFromDatabase() {
            User user = savedUser();

            UserResponse response = userService.getCurrentUser(EMAIL);

            assertThat(response.id()).isEqualTo(user.getId());
            assertThat(response.email()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("Should throw BusinessException when user does not exist")
        void getCurrentUser_notFound_throwsException() {
            assertThatThrownBy(() -> userService.getCurrentUser(EMAIL))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("Should return user from database")
        void getUserById_existingUser_returnsUser() {
            User user = savedUser();

            UserResponse response = userService.getUserById(user.getId());

            assertThat(response.id()).isEqualTo(user.getId());
            assertThat(response.firstName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("updateCurrentUser()")
    class UpdateCurrentUser {

        @Test
        @DisplayName("Should update user in database")
        void updateCurrentUser_existingUser_updatesDatabase() {
            savedUser();

            UserResponse response = userService.updateCurrentUser(
                    EMAIL,
                    new UpdateUserRequest("Jane", "Smith")
            );

            User fromDb = userRepository.findByEmail(EMAIL).orElseThrow();
            assertThat(response.firstName()).isEqualTo("Jane");
            assertThat(fromDb.getFirstName()).isEqualTo("Jane");
            assertThat(fromDb.getLastName()).isEqualTo("Smith");
        }
    }
}
