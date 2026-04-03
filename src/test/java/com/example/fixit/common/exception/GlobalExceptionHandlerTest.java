package com.example.fixit.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("City must not be blank"));

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleIllegalArgument_returnsErrorMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Unknown category: xyz"));

        assertNotNull(response.getBody());
        assertEquals("Unknown category: xyz", response.getBody().message());
    }

    @Test
    void handleIllegalArgument_returnsCorrectStatus() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("test error"));

        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
        assertNull(response.getBody().fieldErrors());
    }
}
