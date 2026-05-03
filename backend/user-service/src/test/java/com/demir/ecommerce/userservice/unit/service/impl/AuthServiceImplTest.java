package com.demir.ecommerce.userservice.unit.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.dto.auth.request.LoginRequest;
import com.demir.ecommerce.userservice.dto.auth.request.RegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.request.SellerRegisterRequest;
import com.demir.ecommerce.userservice.dto.auth.response.AuthResponse;
import com.demir.ecommerce.userservice.entity.RefreshToken;
import com.demir.ecommerce.userservice.entity.Role;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.repository.RefreshTokenRepository;
import com.demir.ecommerce.userservice.repository.SellerProfileRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.JwtService;
import com.demir.ecommerce.userservice.service.impl.AuthServiceImpl;
import com.demir.ecommerce.userservice.util.TokenHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SellerProfileRepository sellerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@test.com";
    private static final String RAW_REFRESH_TOKEN = "refresh-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpirationDays", 7L);
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest("John", "Doe", EMAIL, "secret123");
    }

    private SellerRegisterRequest sellerRegisterRequest() {
        return new SellerRegisterRequest(
                "Jane",
                "Seller",
                EMAIL,
                "secret123",
                "Store",
                "Company",
                "1234567890",
                "Description"
        );
    }

    private User user(boolean active) {
        User user = new User("John", "Doe", EMAIL, "encoded", Set.of(Role.CUSTOMER), active);
        user.setId(USER_ID);
        return user;
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("Should create customer and return tokens")
        void register_newEmail_returnsTokens() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode("secret123")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setId(USER_ID);
                return saved;
            });
            when(jwtService.generateToken(eq(USER_ID), eq(EMAIL), anyList())).thenReturn("access-token");

            AuthResponse response = authService.register(registerRequest());

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isNotBlank();
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when email exists")
        void register_existingEmail_throwsException() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest()))
                    .isInstanceOf(BusinessException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("registerSeller()")
    class RegisterSeller {

        @Test
        @DisplayName("Should create seller user and profile")
        void registerSeller_validRequest_createsSellerProfile() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(sellerProfileRepository.existsByTaxNumber("1234567890")).thenReturn(false);
            when(passwordEncoder.encode("secret123")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setId(USER_ID);
                return saved;
            });
            when(jwtService.generateToken(eq(USER_ID), eq(EMAIL), anyList())).thenReturn("access-token");

            AuthResponse response = authService.registerSeller(sellerRegisterRequest());

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isNotBlank();
            verify(sellerProfileRepository).save(any());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should throw BusinessException when tax number exists")
        void registerSeller_existingTaxNumber_throwsException() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(sellerProfileRepository.existsByTaxNumber("1234567890")).thenReturn(true);

            assertThatThrownBy(() -> authService.registerSeller(sellerRegisterRequest()))
                    .isInstanceOf(BusinessException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("Should return tokens for active user with valid password")
        void login_validCredentials_returnsTokens() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true)));
            when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);
            when(jwtService.generateToken(eq(USER_ID), eq(EMAIL), anyList())).thenReturn("access-token");

            AuthResponse response = authService.login(new LoginRequest(EMAIL, "secret123"));

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isNotBlank();
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for wrong password")
        void login_wrongPassword_throwsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true)));
            when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "wrong")))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Should throw BusinessException when account is disabled")
        void login_disabledAccount_throwsException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(false)));
            when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "secret123")))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTest {

        @Test
        @DisplayName("Should revoke old refresh token and return new tokens")
        void refreshToken_validToken_rotatesToken() {
            RefreshToken storedToken = new RefreshToken(
                    TokenHashUtil.sha256(RAW_REFRESH_TOKEN),
                    user(true),
                    LocalDateTime.now().plusDays(1)
            );
            when(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(RAW_REFRESH_TOKEN)))
                    .thenReturn(Optional.of(storedToken));
            when(jwtService.generateToken(eq(USER_ID), eq(EMAIL), anyList())).thenReturn("new-access-token");

            AuthResponse response = authService.refreshToken(RAW_REFRESH_TOKEN);

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.refreshToken()).isNotBlank();
            assertThat(storedToken.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(storedToken);
        }
    }

    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("Should revoke stored refresh token")
        void logout_validToken_revokesToken() {
            RefreshToken storedToken = new RefreshToken(
                    TokenHashUtil.sha256(RAW_REFRESH_TOKEN),
                    user(true),
                    LocalDateTime.now().plusDays(1)
            );
            when(refreshTokenRepository.findByTokenHash(TokenHashUtil.sha256(RAW_REFRESH_TOKEN)))
                    .thenReturn(Optional.of(storedToken));

            authService.logout(RAW_REFRESH_TOKEN);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertThat(captor.getValue().isRevoked()).isTrue();
        }
    }
}
