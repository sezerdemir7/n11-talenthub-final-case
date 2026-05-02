package com.demir.ecommerce.commonlib.security;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String USER_ID_HEADER    = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();

        String userId = request.getHeader(USER_ID_HEADER);
        String roles  = request.getHeader(USER_ROLES_HEADER);

        if (userId != null && !userId.isBlank()) {
            template.header(USER_ID_HEADER, userId);
        }

        if (roles != null && !roles.isBlank()) {
            template.header(USER_ROLES_HEADER, roles);
        }
    }
}

