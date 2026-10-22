package com.example.rewards.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies how each exception type is translated into an HTTP status and problem detail body. */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundMapsTo404() {
        ProblemDetail body = handler.handleNotFound(new CustomerNotFoundException("missing"));

        assertEquals(404, body.getStatus());
        assertEquals("missing", body.getDetail());
    }

    @Test
    void invalidRequestMapsTo400() {
        ProblemDetail body = handler.handleInvalidRequest(new InvalidDateRangeException("bad range"));

        assertEquals(400, body.getStatus());
        assertEquals("bad range", body.getDetail());
    }

    @Test
    void unexpectedErrorMapsTo500AndHidesTheMessage() {
        ProblemDetail body = handler.handleUnexpected(new RuntimeException("secret internals"));

        assertEquals(500, body.getStatus());
        assertEquals("Unexpected error", body.getDetail());
    }
}
