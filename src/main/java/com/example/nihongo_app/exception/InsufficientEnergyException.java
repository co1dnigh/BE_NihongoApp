package com.example.nihongo_app.exception;

/**
 * Exception nghiệp vụ khi user không đủ năng lượng để bắt đầu bài.
 * Trả về HTTP 400 vì đây là điều kiện hợp lệ nhưng chưa thoả mãn (có thể khắc phục
 * bằng cách đợi hồi năng lượng hoặc mua thêm).
 */
public class InsufficientEnergyException extends RuntimeException {
    public InsufficientEnergyException(String message) {
        super(message);
    }
}