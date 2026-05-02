package com.demir.ecommerce.commonlib.security;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.excepption.message.GeneralErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

public class SecurityUtils {

    private static final String USER_ID_HEADER    = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    private SecurityUtils() {}

    public static boolean isOwner(Long resourceOwnerId) {
        return getUserId().equals(resourceOwnerId);
    }

    public static boolean isAdmin() {
        return getRoles().contains("ADMIN");
    }

    public static boolean isOwnerOrAdmin(Long resourceOwnerId) {
        return isOwner(resourceOwnerId) || isAdmin();
    }


    public static Long getUserId() {
        String userId = getRequest().getHeader(USER_ID_HEADER);

        if (userId == null || userId.isBlank()) {
            throw new BusinessException(GeneralErrorMessage.UNAUTHORIZED);
        }

        return Long.parseLong(userId);
    }
    public static boolean hasRole(String role) {
        return getRoles().contains(role);
    }


    private static List<String> getRoles() {
        String roles = getRequest().getHeader(USER_ROLES_HEADER);

        return (roles != null && !roles.isBlank())
                ? List.of(roles.split(","))
                : List.of();
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new BusinessException(GeneralErrorMessage.UNAUTHORIZED);
        }

        return attributes.getRequest();
    }


}