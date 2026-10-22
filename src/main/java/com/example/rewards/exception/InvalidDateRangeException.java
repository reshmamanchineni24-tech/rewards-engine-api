package com.example.rewards.exception;

/** Thrown when the requested start date is after the end date. Mapped to HTTP 400 by GlobalExceptionHandler. */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
