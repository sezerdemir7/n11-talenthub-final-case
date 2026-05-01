package com.demir.ecommerce.userservice.exception.message;


import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum UserErrorMessage implements BaseErrorMessage {

    USER_NOT_FOUND("USER_NOT_FOUND", "User not found.", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User already exists.", HttpStatus.CONFLICT),
    SELLER_PROFILE_NOT_FOUND("SELLER_PROFILE_NOT_FOUND", "Seller profile not found.", HttpStatus.NOT_FOUND),
    INVALID_SELLER_STATUS("INVALID_SELLER_STATUS", "Invalid seller status.", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND("ADDRESS_NOT_FOUND", "Address not found.", HttpStatus.NOT_FOUND),
    INVALID_ADDRESS("INVALID_ADDRESS", "Invalid address.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    UserErrorMessage(String code, String message, HttpStatus status) {
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
