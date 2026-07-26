package com.example.nihongo_app.exception;

/**
 * Exception nghiệp vụ khi user chưa mở khoá bài học mà cố gắng bắt đầu.
 * Trả về HTTP 403 (không phải 404 vì bài học tồn tại nhưng không có quyền truy cập).
 */
public class LessonLockedException extends RuntimeException {
    public LessonLockedException(String message) {
        super(message);
    }
}