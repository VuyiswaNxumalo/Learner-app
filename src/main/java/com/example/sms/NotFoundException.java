package com.example.sms;

public class NotFoundException extends IllegalArgumentException {
    public NotFoundException(String message) {
        super(message);
    }
}