package com.apanaswastha.erp.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private String timestamp;
    private String path;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data, String path) {
        ApiResponse<T> response = success(message, data);
        response.setPath(path);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(Instant.now().toString())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, "GENERIC_ERROR", null);
    }
}
