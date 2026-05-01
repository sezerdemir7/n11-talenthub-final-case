package com.demir.ecommerce.productservice.exception.message;

import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum StorageErrorMessage implements BaseErrorMessage {

    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "File upload failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("FILE_DELETE_FAILED", "File delete failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE("INVALID_FILE", "Invalid file.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "Only image files are allowed.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    StorageErrorMessage(String code, String message, HttpStatus status) {
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