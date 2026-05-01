package com.demir.ecommerce.commonlib.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {

    private final String message;
    private final String errorCode;
    private final String path;
    private final int status;
    private final Instant timestamp;
    private final Map<String, String> validationErrors;

    private ErrorResponse(String message,
                          String errorCode,
                          String path,
                          int status,
                          Map<String, String> validationErrors) {
        validate(message, errorCode, path, status);

        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
        this.status = status;
        this.timestamp = Instant.now();
        this.validationErrors = validationErrors != null
                ? Collections.unmodifiableMap(validationErrors)
                : null;
    }

    public static ErrorResponse of(String message, String errorCode, String path, int status) {
        return new ErrorResponse(message, errorCode, path, status, null);
    }

    public static ErrorResponse of(String message,
                                   String errorCode,
                                   String path,
                                   int status,
                                   Map<String, String> validationErrors) {
        return new ErrorResponse(message, errorCode, path, status, validationErrors);
    }

    private static void validate(String message, String errorCode, String path, int status) {
        if (message == null || message.isBlank()) {
            throw new IllegalStateException("ErrorResponse.message must not be blank");
        }
        if (errorCode == null || errorCode.isBlank()) {
            throw new IllegalStateException("ErrorResponse.errorCode must not be blank");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("ErrorResponse.path must not be blank");
        }
        if (status < 100 || status > 599) {
            throw new IllegalStateException("ErrorResponse.status must be a valid HTTP status code");
        }
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getPath() {
        return path;
    }

    public int getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}