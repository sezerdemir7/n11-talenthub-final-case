package com.demir.ecommerce.userservice.exception;

import com.demir.ecommerce.commonlib.dto.ErrorResponse;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.exception.message.AuthErrorMessage;
import com.demir.ecommerce.userservice.exception.message.GeneralErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        log.warn("Business exception occurred. Code: {}, Message: {}, Path: {}",
                ex.getCode(),
                ex.getErrorMessage(),
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.valueOf(ex.getHttpStatus()),
                ex.getErrorMessage(),
                ex.getCode(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation error occurred. Path: {}, Errors: {}",
                request.getRequestURI(),
                validationErrors
        );

        ErrorResponse response = ErrorResponse.of(
                GeneralErrorMessage.VALIDATION_ERROR.getMessage(),
                GeneralErrorMessage.VALIDATION_ERROR.getCode(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                validationErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Constraint violation occurred. Path: {}, Message: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                GeneralErrorMessage.VALIDATION_ERROR.getMessage(),
                GeneralErrorMessage.VALIDATION_ERROR.getCode(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        log.warn("Bad credentials attempt. Path: {}",
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                AuthErrorMessage.INVALID_CREDENTIALS.getMessage(),
                AuthErrorMessage.INVALID_CREDENTIALS.getCode(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed. Path: {}, Message: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                GeneralErrorMessage.UNAUTHORIZED.getMessage(),
                GeneralErrorMessage.UNAUTHORIZED.getCode(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied. Path: {}, Message: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                GeneralErrorMessage.ACCESS_DENIED.getMessage(),
                GeneralErrorMessage.ACCESS_DENIED.getCode(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error occurred. Path: {}",
                request.getRequestURI(),
                ex
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                GeneralErrorMessage.INTERNAL_SERVER_ERROR.getMessage(),
                GeneralErrorMessage.INTERNAL_SERVER_ERROR.getCode(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,
                                                             String message,
                                                             String errorCode,
                                                             String path) {
        return ResponseEntity.status(status).body(
                ErrorResponse.of(
                        message,
                        errorCode,
                        path,
                        status.value()
                )
        );
    }
}