package com.nihongoapp.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String correlationId() {
        return MDC.get("correlationId");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_REQUEST", msg, correlationId()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_REQUEST", ex.getMessage(), correlationId()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_REQUEST", ex.getMessage(), correlationId()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_REQUEST", "Du lieu yeu cau khong hop le.", correlationId()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case "INSUFFICIENT_BALANCE" -> HttpStatus.PAYMENT_REQUIRED;
            case "BANNER_INACTIVE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "IDEMPOTENCY_CONFLICT" -> HttpStatus.CONFLICT;
            case "RATE_LIMITED" -> HttpStatus.TOO_MANY_REQUESTS;
            case "ACCOUNT_BANNED" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), correlationId()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("UNAUTHORIZED", "Khong co quyen truy cap.", correlationId()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        String supported = ex.getSupportedMethods() != null ? String.join(", ", ex.getSupportedMethods()) : "POST";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of("METHOD_NOT_ALLOWED",
                        "Method not supported. Use " + supported, correlationId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "Loi he thong: " + ex.getMessage(), correlationId()));
    }
}
