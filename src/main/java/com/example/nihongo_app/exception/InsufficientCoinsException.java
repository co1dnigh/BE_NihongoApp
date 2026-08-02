package com.example.nihongo_app.exception;

public class InsufficientCoinsException extends RuntimeException {
    public InsufficientCoinsException(String message) {
        super(message);
    }
}
