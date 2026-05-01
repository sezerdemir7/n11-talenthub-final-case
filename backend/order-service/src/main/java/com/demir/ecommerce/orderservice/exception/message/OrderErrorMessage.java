package com.demir.ecommerce.orderservice.exception.message;

import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;
import org.springframework.http.HttpStatus;

public enum OrderErrorMessage implements BaseErrorMessage {

    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found.", HttpStatus.NOT_FOUND),

    ORDER_ITEM_NOT_FOUND("ORDER_ITEM_NOT_FOUND", "Order item not found.", HttpStatus.NOT_FOUND),

    ORDER_IS_EMPTY("ORDER_IS_EMPTY", "Order has no items.", HttpStatus.BAD_REQUEST),

    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", "Invalid order status.", HttpStatus.BAD_REQUEST),

    ORDER_CREATION_FAILED("ORDER_CREATION_FAILED", "Order could not be created.", HttpStatus.INTERNAL_SERVER_ERROR),


    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found in product service.", HttpStatus.NOT_FOUND),

    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "Not enough stock for product.", HttpStatus.BAD_REQUEST),

    PRODUCT_INACTIVE("PRODUCT_INACTIVE", "Product is not active.", HttpStatus.BAD_REQUEST),

    CART_NOT_FOUND("CART_NOT_FOUND", "Cart not found for user.", HttpStatus.NOT_FOUND),

    CART_EMPTY("CART_EMPTY", "Cart is empty.", HttpStatus.BAD_REQUEST),

    PRODUCT_SERVICE_UNAVAILABLE("PRODUCT_SERVICE_UNAVAILABLE", "Product service is unavailable.", HttpStatus.SERVICE_UNAVAILABLE),

    CART_SERVICE_UNAVAILABLE("CART_SERVICE_UNAVAILABLE", "Cart service is unavailable.", HttpStatus.SERVICE_UNAVAILABLE),
    PAYMENT_SERVICE_UNAVAILABLE("PAYMENT_SERVICE_UNAVAILABLE", "Payment service is unavailable.", HttpStatus.SERVICE_UNAVAILABLE),
    PAYMENT_FAILED("PAYMENT_FAILED", "Payment processing failed.", HttpStatus.BAD_REQUEST);
    private final String code;
    private final String message;
    private final HttpStatus status;

    OrderErrorMessage(String code, String message, HttpStatus status) {
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