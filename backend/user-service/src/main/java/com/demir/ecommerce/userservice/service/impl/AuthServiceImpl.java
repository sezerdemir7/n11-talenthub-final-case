package com.demir.ecommerce.userservice.service.impl;

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
import com.demir.ecommerce.userservice.exception.message.AuthErrorMessage;
import com.demir.ecommerce.userservice.exception.message.UserErrorMessage;
import com.demir.ecommerce.userservice.repository.RefreshTokenRepository;
import com.demir.ecommerce.userservice.repository.SellerProfileRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.AuthService;
import com.demir.ecommerce.userservice.service.JwtService;
import com.demir.ecommerce.userservice.util.TokenHashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           SellerProfileRepository sellerProfileRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(AuthErrorMessage.EMAIL_ALREADY_EXISTS);
        }

        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Set.of(Role.CUSTOMER),
                true
        );

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                toRoleNames(savedUser)
        );

        String refreshToken = createRefreshToken(savedUser);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse registerSeller(SellerRegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(AuthErrorMessage.EMAIL_ALREADY_EXISTS);
        }

        if (sellerProfileRepository.existsByTaxNumber(request.taxNumber())) {
            throw new BusinessException(AuthErrorMessage.TAX_NUMBER_ALREADY_EXISTS);
        }

        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Set.of(Role.CUSTOMER, Role.SELLER),
                true
        );

        User savedUser = userRepository.save(user);

        SellerProfile sellerProfile = new SellerProfile();
        sellerProfile.setUser(savedUser);
        sellerProfile.setStoreName(request.storeName());
        sellerProfile.setCompanyName(request.companyName());
        sellerProfile.setTaxNumber(request.taxNumber());
        sellerProfile.setStoreDescription(request.storeDescription());
        sellerProfile.setStatus(SellerStatus.APPROVED);
        sellerProfile.setIsVerified(false);

        sellerProfileRepository.save(sellerProfile);

        String accessToken = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                toRoleNames(savedUser)
        );

        String refreshToken = createRefreshToken(savedUser);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessException(AuthErrorMessage.ACCOUNT_DISABLED);
        }

        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                toRoleNames(user)
        );

        String refreshToken = createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {

        String tokenHash = TokenHashUtil.sha256(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(AuthErrorMessage.INVALID_REFRESH_TOKEN));

        if (!storedToken.isValid()) {
            throw new BusinessException(AuthErrorMessage.INVALID_REFRESH_TOKEN);
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();

        String newAccessToken = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                toRoleNames(user)
        );

        String newRefreshToken = createRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {

        String tokenHash = TokenHashUtil.sha256(refreshToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(AuthErrorMessage.INVALID_REFRESH_TOKEN));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
    }

    private List<String> toRoleNames(User user) {
        return user.getRoles()
                .stream()
                .map(Enum::name)
                .toList();
    }

    private String createRefreshToken(User user) {

        String rawToken = generateSecureToken();
        String tokenHash = TokenHashUtil.sha256(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                tokenHash,
                user,
                LocalDateTime.now().plusDays(refreshExpirationDays)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
