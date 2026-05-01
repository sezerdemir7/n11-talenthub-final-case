package com.demir.ecommerce.orderservice.exception;

import com.demir.ecommerce.commonlib.dto.ErrorResponse;
import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.excepption.message.GeneralErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
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

        log.warn("Business exception. Code: {}, Message: {}, Path: {}",
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
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation error. Path: {}, Errors: {}",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.badRequest().body(
                ErrorResponse.of(
                        GeneralErrorMessage.VALIDATION_ERROR.getMessage(),
                        GeneralErrorMessage.VALIDATION_ERROR.getCode(),
                        request.getRequestURI(),
                        HttpStatus.BAD_REQUEST.value(),
                        errors
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        log.warn("Constraint violation. Path: {}, Message: {}",
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
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

        log.error("Unexpected error. Path: {}", request.getRequestURI(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                GeneralErrorMessage.INTERNAL_SERVER_ERROR.getMessage(),
                GeneralErrorMessage.INTERNAL_SERVER_ERROR.getCode(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            String code,
            String path
    ) {
        return ResponseEntity.status(status).body(
                ErrorResponse.of(
                        message,
                        code,
                        path,
                        status.value()
                )
        );
    }
}