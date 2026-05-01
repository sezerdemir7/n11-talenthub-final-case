package com.demir.ecommerce.userservice.exception.message;


import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum TokenErrorMessage implements BaseErrorMessage {

    INVALID_TOKEN("INVALID_TOKEN", "Invalid token.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token expired.", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED("TOKEN_GENERATION_FAILED", "Unable to generate token.", HttpStatus.INTERNAL_SERVER_ERROR),

    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "Invalid refresh token.", HttpStatus.UNAUTHORIZED),
    EMAIL_VERIFICATION_TOKEN_INVALID("EMAIL_VERIFICATION_TOKEN_INVALID", "Invalid email verification token.", HttpStatus.BAD_REQUEST),
    EMAIL_VERIFICATION_TOKEN_EXPIRED("EMAIL_VERIFICATION_TOKEN_EXPIRED", "Email verification token expired.", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_INVALID("PASSWORD_RESET_TOKEN_INVALID", "Invalid password reset token.", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_EXPIRED("PASSWORD_RESET_TOKEN_EXPIRED", "Password reset token expired.", HttpStatus.BAD_REQUEST),

    AUTHORIZATION_HEADER_MISSING("AUTHORIZATION_HEADER_MISSING", "Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus status;

    TokenErrorMessage(String code, String message, HttpStatus status) {
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
