package com.demir.ecommerce.orderservice.entity;

public enum OrderStatus {
    PENDING,
    WAITING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    EXPIRED
}
