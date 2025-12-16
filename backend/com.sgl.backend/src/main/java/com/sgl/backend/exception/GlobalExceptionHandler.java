package com.sgl.backend.exception;

import com.sgl.backend.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SglAuthException.class)
    public ResponseEntity<ErrorResponse> handleSglAuthException(SglAuthException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SglException.class)
    public ResponseEntity<ErrorResponse> handleSglException(SglException ex) {
        HttpStatus status = ex.getStatus();
        ErrorResponse error = new ErrorResponse(status.value(), ex.getMessage());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
