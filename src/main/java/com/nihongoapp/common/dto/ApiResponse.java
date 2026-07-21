package com.nihongoapp.common.dto;

public record ApiResponse<T>(String code, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", "Success", data);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
