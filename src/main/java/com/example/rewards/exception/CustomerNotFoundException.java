package com.example.rewards.exception;

/** Thrown when a customer has no recorded transactions. Mapped to HTTP 404 by GlobalExceptionHandler. */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
