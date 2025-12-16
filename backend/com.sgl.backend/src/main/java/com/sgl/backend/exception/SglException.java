package com.sgl.backend.exception;

import org.springframework.http.HttpStatus;

public class SglException extends RuntimeException {
    private final HttpStatus status;

    public SglException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST; 
    }

    public SglException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public SglException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
