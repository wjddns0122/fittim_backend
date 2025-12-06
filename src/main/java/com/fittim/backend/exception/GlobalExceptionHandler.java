package com.fittim.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body("Validation error: " + e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        // Specifically for login failures which throw IllegalArgumentException with
        // this message
        if ("Invalid email or password".equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
