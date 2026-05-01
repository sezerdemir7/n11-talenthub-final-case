package com.demir.ecommerce.commonlib.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class RestResponse<T> implements Serializable {

    private final T data;
    private final boolean success;
    private final String message;
    private final Instant timestamp;

    private RestResponse(T data, boolean success, String message) {
        this.data = data;
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public static <T> RestResponse<T> of(T data) {
        return new RestResponse<>(data, true, null);
    }

    public static <T> RestResponse<T> of(T data, String message) {
        return new RestResponse<>(data, true, message);
    }

    public static RestResponse<Void> success(String message) {
        return new RestResponse<>(null, true, message);
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
