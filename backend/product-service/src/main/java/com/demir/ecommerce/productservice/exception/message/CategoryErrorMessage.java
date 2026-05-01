package com.demir.ecommerce.productservice.exception.message;


import com.demir.ecommerce.commonlib.excepption.BaseErrorMessage;

public enum CategoryErrorMessage implements BaseErrorMessage {

    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Category not found.", 404),
    CATEGORY_PARENT_CANNOT_BE_SELF("CATEGORY_PARENT_CANNOT_BE_SELF", "Category parent cannot be itself.", 400);

    private final String code;
    private final String message;
    private final int status;

    CategoryErrorMessage(String code, String message, int status) {
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