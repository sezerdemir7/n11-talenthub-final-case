package com.demir.ecommerce.notificationservice.dto;

public record NotificationMessage(
        String type,
        String message,
        Object data,
        Long notificationId
) {
}