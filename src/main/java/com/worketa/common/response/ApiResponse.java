package com.worketa.common.response;

import java.time.OffsetDateTime;

public record ApiResponse<T>(boolean success, String message, T data, OffsetDateTime timestamp) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, OffsetDateTime.now());
    }
}
