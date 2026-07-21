package com.nihongoapp.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        String correlationId,
        LocalDateTime timestamp) {

    public static ErrorResponse of(String errorCode, String message, String correlationId) {
        return new ErrorResponse(errorCode, message, correlationId, LocalDateTime.now());
    }
}
