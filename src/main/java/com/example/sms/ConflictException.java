package com.example.sms;

public class ConflictException extends IllegalArgumentException {
    public ConflictException(String message) {
        super(message);
    }
}