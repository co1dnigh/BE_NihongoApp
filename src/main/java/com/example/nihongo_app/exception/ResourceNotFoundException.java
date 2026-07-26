package com.example.nihongo_app.exception;

/**
 * Exception nghiệp vụ khi không tìm thấy resource (lesson, topic, ...).
 * Trả về HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}