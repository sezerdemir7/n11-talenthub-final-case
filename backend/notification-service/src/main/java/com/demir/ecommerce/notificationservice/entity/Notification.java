package com.demir.ecommerce.notificationservice.entity;

import com.demir.ecommerce.commonlib.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {


    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type;        // ORDER_CREATED, ORDER_CANCELLED vs.

    @Column(nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String data;        // JSON string — orderId, totalPrice vs.

    @Column(nullable = false)
    private boolean isRead = false;



    public Notification() {}

    public Notification(Long userId, String type, String message, String data) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.data = data;
    }


    public Long getUserId() { return userId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getData() { return data; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }
}