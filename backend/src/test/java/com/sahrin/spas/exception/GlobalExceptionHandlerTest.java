package com.sahrin.spas.exception;

import com.sahrin.spas.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("IllegalArgumentException with message returns 400 Bad Request and INVALID_ARGUMENT error code")
    void handleIllegalArgumentException_withMessage_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Score must be between 0.00 and 100.00.");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Score must be between 0.00 and 100.00.", response.getBody().getMessage());
        assertEquals("INVALID_ARGUMENT", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("IllegalArgumentException with null message returns 400 Bad Request and fallback message")
    void handleIllegalArgumentException_nullMessage_returnsFallback400() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid request argument provided.", response.getBody().getMessage());
        assertEquals("INVALID_ARGUMENT", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("IllegalArgumentException with blank message returns 400 Bad Request and fallback message")
    void handleIllegalArgumentException_blankMessage_returnsFallback400() {
        IllegalArgumentException ex = new IllegalArgumentException("   ");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid request argument provided.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("DataIntegrityViolationException returns 409 Conflict")
    void handleDataIntegrityViolationException_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("unique constraint violated");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    @DisplayName("DuplicateResourceException returns 409 Conflict")
    void handleDuplicateResourceException_returns409() {
        DuplicateResourceException ex = new DuplicateResourceException("Student is already enrolled.");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleDuplicateResourceException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Student is already enrolled.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("InvalidStatusTransitionException returns 400 Bad Request")
    void handleInvalidStatusTransitionException_returns400() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("Cannot suspend pending user.");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleInvalidStatusTransitionException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("INVALID_STATUS_TRANSITION", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("ResourceNotFoundException returns 404 Not Found")
    void handleResourceNotFoundException_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found.");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("AccountSuspendedException returns 403 Forbidden")
    void handleAccountSuspendedException_returns403() {
        AccountSuspendedException ex = new AccountSuspendedException("Account suspended.");
        ResponseEntity<ApiErrorResponse> response = exceptionHandler.handleAccountSuspendedException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("ACCOUNT_SUSPENDED", response.getBody().getErrorCode());
    }
}
