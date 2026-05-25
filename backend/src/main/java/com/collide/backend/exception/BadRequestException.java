package com.collide.backend.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
