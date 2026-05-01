package com.demir.ecommerce.userservice.util;

import com.demir.ecommerce.userservice.security.CustomUserDetails;
import com.demir.ecommerce.userservice.exception.UnauthorizedAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedAccessException("Geçerli kullanıcı bulunamadı.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new UnauthorizedAccessException("Geçerli kullanıcı bulunamadı.");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getUsername();
    }

    public static void checkOwner(Long ownerId) {
        Long currentUserId = getCurrentUserId();

        if (!currentUserId.equals(ownerId)) {
            throw new AccessDeniedException("Kendi kullanıcı kimliğin dışında işlem yapamazsın.");
        }
    }
}
