package com.example.nihongo_app.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bắt tất cả exception ném ra từ controller và trả về JSON có cấu trúc rõ ràng,
 * thay vì để Spring Boot forward sang "/error" (nơi Security có thể chặn và
 * trả về 403 gây hiểu nhầm, như trường hợp lỗi field JSON sai tên vừa gặp).
 *
 * Kế thừa ResponseEntityExceptionHandler để override đúng handler có sẵn cho
 * MethodArgumentNotValidException/HttpMessageNotReadableException, tránh xung đột.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ===== 1. Lỗi validate @Valid trên DTO (vd thiếu field required, sai kiểu) =====
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
                        (a, b) -> a
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors));
    }

    // ===== 2. Body JSON không parse được (sai cú pháp, sai kiểu dữ liệu, field lạ...) =====
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST,
                        "Request body không hợp lệ hoặc sai định dạng JSON", null));
    }

    // ===== 3. Lỗi validate ở tầng service/param (vd @Validated trên @PathVariable) =====
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    // ===== 3b. Sai tham số nghiệp vụ (vd thiếu multipart file, sai enum) =====
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    // ===== 4. Vi phạm ràng buộc DB (trùng unique key, foreign key không tồn tại...) =====
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildBody(HttpStatus.CONFLICT,
                        "Dữ liệu vi phạm ràng buộc (trùng lặp hoặc tham chiếu không hợp lệ)", null));
    }

    // ===== 5. Có JWT hợp lệ nhưng không đủ quyền (vd role USER gọi API admin) =====
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(buildBody(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này", null));
    }

    // ===== 6. Exception nghiệp vụ tự định nghĩa, vd NotFoundException của bạn =====
    // Ví dụ mẫu, sửa lại theo exception thật của bạn nếu có:
    /*
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }
    */

    // ===== 7. Bắt tất cả lỗi còn sót lại (fallback cuối cùng, tránh lộ stack trace) =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        // Log lỗi thật ra console để debug, không trả stack trace cho client
        logger.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi xảy ra phía server", null));
    }

    // ===== Helper dựng body JSON đồng nhất cho mọi lỗi =====
    private Map<String, Object> buildBody(HttpStatus status, String message, Map<String, String> errors) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }
        return body;
    }
}
