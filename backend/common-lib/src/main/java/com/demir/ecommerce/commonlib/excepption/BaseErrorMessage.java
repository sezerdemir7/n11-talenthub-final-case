package com.demir.ecommerce.commonlib.excepption;



public interface BaseErrorMessage {
    String getCode();
    String getMessage();
    int  getStatus();
}