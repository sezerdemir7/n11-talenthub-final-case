package com.demir.ecommerce.userservice.util;

import com.demir.ecommerce.commonlib.dto.ErrorResponse;
import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class SecurityResponseUtil {

    private SecurityResponseUtil() {
    }

    public static void writeErrorResponse(HttpServletResponse response,
                                          ObjectMapper objectMapper,
                                          BaseErrorMessage errorMessage,
                                          String path) throws IOException {
        response.setStatus(errorMessage.getStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        ErrorResponse errorResponse = ErrorResponse.of(
                errorMessage.getMessage(),
                errorMessage.getCode(),
                path,
                errorMessage.getStatus()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}