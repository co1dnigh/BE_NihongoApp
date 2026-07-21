package com.nihongoapp.common.util;

import com.nihongoapp.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class RsUtil {

    public static <T> ResponseEntity<ApiResult<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResult.ok(data, message));
    }

    public static <T> ResponseEntity<ApiResult<T>> ok(T data) {
        return ResponseEntity.ok(ApiResult.ok(data, "OK"));
    }

    public static <T> ResponseEntity<ApiResult<T>> fail(String errorCode, String message) {
        return ResponseEntity.ok(ApiResult.fail(errorCode, message));
    }

    public static <T> ResponseEntity<ApiResult<T>> fail(String errorCode, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(ApiResult.fail(errorCode, message));
    }

    public static ResponseEntity<ErrorResponse> error(String errorCode, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(ErrorResponse.of(errorCode, message, ""));
    }

    private RsUtil() {}
}
