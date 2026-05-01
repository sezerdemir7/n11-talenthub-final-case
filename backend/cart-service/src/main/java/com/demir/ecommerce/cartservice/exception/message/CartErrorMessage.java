package com.demir.ecommerce.cartservice.exception.message;

import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum CartErrorMessage implements BaseErrorMessage {

    CART_NOT_FOUND("CART_NOT_FOUND", "Cart not found.", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", "Cart item not found.", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_AVAILABLE("PRODUCT_NOT_AVAILABLE", "Product is not available.", HttpStatus.BAD_REQUEST),
    CART_IS_EMPTY("CART_IS_EMPTY", "Cart is empty.", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY("INVALID_QUANTITY", "Quantity must be greater than 0.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    CartErrorMessage(String code, String message, HttpStatus status) {
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