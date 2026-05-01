package com.demir.ecommerce.commonlib.excepption.message;


import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;

public enum GeneralErrorMessage implements BaseErrorMessage {

    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Unexpected error occurred.", 500),
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation error occurred.", 400),
    BAD_REQUEST("BAD_REQUEST", "Invalid request.", 400),
    ACCESS_DENIED("ACCESS_DENIED", "You do not have permission.", 403),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication required.", 401);
    private final String code;
    private final String message;
    private final int status;

    GeneralErrorMessage(String code, String message, int status) {
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
        return status;
    }
}

