package com.sgl.backend.exception;

public class SglAuthException extends RuntimeException {
    public SglAuthException(String message) {
        super(message);
    }

    public SglAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
