package com.demir.ecommerce.commonlib.excepption;




public class BusinessException extends RuntimeException {

    private final BaseErrorMessage errorMessage;

    public BusinessException(BaseErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    public String getCode() {
        return errorMessage.getCode();
    }

    public String getErrorMessage() {
        return errorMessage.getMessage();
    }

    public int getHttpStatus() {
        return errorMessage.getStatus();
    }
}
