package com.demir.ecommerce.userservice.exception.message;

import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum GeneralErrorMessage implements BaseErrorMessage {

    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation error occurred.", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", "Invalid request.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("ACCESS_DENIED", "You do not have permission.", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication required.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    GeneralErrorMessage(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status.value();
    }
}
