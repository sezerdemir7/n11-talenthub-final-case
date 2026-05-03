package com.demir.ecommerce.userservice.integration.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.dto.auth.request.LoginRequest;
import com.demir.ecommerce.userservice.dto.auth.request.RegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.request.SellerRegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.response.AuthResponse;
import com.demir.ecommerce.userservice.entity.RefreshToken;
import com.demir.ecommerce.userservice.entity.Role;
import com.demir.ecommerce.userservice.entity.SellerProfile;
import com.demir.ecommerce.userservice.entity.SellerStatus;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.repository.RefreshTokenRepository;
import com.demir.ecommerce.userservice.repository.SellerProfileRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.AuthService;
import com.demir.ecommerce.userservice.service.JwtService;
import com.demir.ecommerce.userservice.util.TokenHashUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AuthServiceImpl Integration Tests")
class AuthServiceImplIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private static final String EMAIL = "auth@test.com";

    @AfterEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        sellerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest("John", "Doe", EMAIL, "secret123");
    }

    private SellerRegisterRequest sellerRegisterRequest() {
        return new SellerRegisterRequest(
                "Jane",
                "Seller",
                "seller-auth@test.com",
                "secret123",
                "Store",
                "Company",
                "1234567890",
                "Description"
        );
    }

    private User savedUser(boolean active) {
        return userRepository.save(
                new User("John", "Doe", EMAIL, passwordEncoder.encode("secret123"), Set.of(Role.CUSTOMER), active)
        );
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Should persist customer and refresh token")
        void register_newEmail_persistsUserAndRefreshToken() {
            AuthResponse response = authService.register(registerRequest());

            User user = userRepository.findByEmail(EMAIL).orElseThrow();
            assertThat(response.accessToken()).isNotBlank();
            assertThat(response.refreshToken()).isNotBlank();
            assertThat(jwtService.extractEmail(response.accessToken())).isEqualTo(EMAIL);
            assertThat(user.getRoles()).containsExactly(Role.CUSTOMER);
            assertThat(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(response.refreshToken()))).isPresent();
        }

        @Test
        @DisplayName("Should throw BusinessException when email already exists")
        void register_existingEmail_throwsException() {
            savedUser(true);

            assertThatThrownBy(() -> authService.register(registerRequest()))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("registerSeller()")
    class RegisterSeller {

        @Test
        @DisplayName("Should persist seller user and approved profile")
        void registerSeller_validRequest_persistsSellerProfile() {
            AuthResponse response = authService.registerSeller(sellerRegisterRequest());

            User user = userRepository.findByEmail("seller-auth@test.com").orElseThrow();
            Optional<SellerProfile> profile = sellerProfileRepository.findByUserId(user.getId());

            assertThat(response.accessToken()).isNotBlank();
            assertThat(user.getRoles()).contains(Role.CUSTOMER, Role.SELLER);
            assertThat(profile).isPresent();
            assertThat(profile.orElseThrow().getStatus()).isEqualTo(SellerStatus.APPROVED);
            assertThat(profile.orElseThrow().getIsVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Should return tokens for valid credentials")
        void login_validCredentials_returnsTokens() {
            savedUser(true);

            AuthResponse response = authService.login(new LoginRequest(EMAIL, "secret123"));

            assertThat(response.accessToken()).isNotBlank();
            assertThat(response.refreshToken()).isNotBlank();
            assertThat(jwtService.extractEmail(response.accessToken())).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid password")
        void login_invalidPassword_throwsException() {
            savedUser(true);

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "wrong123")))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Should throw BusinessException for disabled account")
        void login_disabledAccount_throwsException() {
            savedUser(false);

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "secret123")))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTest {

        @Test
        @DisplayName("Should revoke old token and create new refresh token")
        void refreshToken_validToken_rotatesRefreshToken() {
            savedUser(true);
            AuthResponse login = authService.login(new LoginRequest(EMAIL, "secret123"));

            AuthResponse refreshed = authService.refreshToken(login.refreshToken());

            RefreshToken oldToken = refreshTokenRepository
                    .findByTokenHash(TokenHashUtil.sha256(login.refreshToken()))
                    .orElseThrow();
            assertThat(oldToken.isRevoked()).isTrue();
            assertThat(refreshed.accessToken()).isNotBlank();
            assertThat(refreshed.refreshToken()).isNotEqualTo(login.refreshToken());
            assertThat(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(refreshed.refreshToken()))).isPresent();
        }
    }

    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("Should revoke refresh token")
        void logout_validToken_revokesToken() {
            savedUser(true);
            AuthResponse login = authService.login(new LoginRequest(EMAIL, "secret123"));

            authService.logout(login.refreshToken());

            RefreshToken token = refreshTokenRepository
                    .findByTokenHash(TokenHashUtil.sha256(login.refreshToken()))
                    .orElseThrow();
            assertThat(token.isRevoked()).isTrue();
        }
    }
}
