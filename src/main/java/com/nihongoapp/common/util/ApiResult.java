package com.nihongoapp.common.util;

public record ApiResult<T>(
        boolean success,
        T data,
        String message,
        String errorCode
) {
    public static <T> ApiResult<T> ok(T data, String message) {
        return new ApiResult<>(true, data, message, null);
    }

    public static <T> ApiResult<T> fail(String errorCode, String message) {
        return new ApiResult<>(false, null, message, errorCode);
    }
}
