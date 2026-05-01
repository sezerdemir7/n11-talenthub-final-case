package com.demir.ecommerce.productservice.exception.message;

import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum ProductErrorMessage implements BaseErrorMessage {

    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found.", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Category not found.", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS("CATEGORY_ALREADY_EXISTS", "Category already exists.", HttpStatus.CONFLICT),
    PRODUCT_ALREADY_EXISTS("PRODUCT_ALREADY_EXISTS", "Product already exists.", HttpStatus.CONFLICT),
    INVALID_CATEGORY_RELATION("INVALID_CATEGORY_RELATION", "Invalid category parent relation.", HttpStatus.BAD_REQUEST),
    SLUG_INPUT_EMPTY("SLUG_INPUT_EMPTY", "Input for slug generation cannot be null or empty.", HttpStatus.BAD_REQUEST),
    PRODUCT_ACCESS_DENIED("PRODUCT_ACCESS_DENIED", "You do not have permission to access this product.", HttpStatus.FORBIDDEN);
    private final String code;
    private final String message;
    private final HttpStatus status;

    ProductErrorMessage(String code, String message, HttpStatus status) {
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