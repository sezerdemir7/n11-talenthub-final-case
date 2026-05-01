package com.demir.ecommerce.userservice.exception.message;


import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum AuthErrorMessage implements BaseErrorMessage {

    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Account is disabled", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    TAX_NUMBER_ALREADY_EXISTS("TAX_NUMBER_ALREADY_EXISTS", "Tax number already exists", HttpStatus.CONFLICT);



    private final String code;
    private final String message;
    private final HttpStatus status;

    AuthErrorMessage(String code, String message, HttpStatus status) {
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