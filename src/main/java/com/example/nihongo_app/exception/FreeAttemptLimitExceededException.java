package com.example.nihongo_app.exception;

public class FreeAttemptLimitExceededException extends RuntimeException {
    public FreeAttemptLimitExceededException(String message) {
        super(message);
    }
}
