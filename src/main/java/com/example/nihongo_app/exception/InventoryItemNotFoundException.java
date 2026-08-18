package com.example.nihongo_app.exception;

public class InventoryItemNotFoundException extends RuntimeException {

    public InventoryItemNotFoundException(String message) {
        super(message);
    }
}