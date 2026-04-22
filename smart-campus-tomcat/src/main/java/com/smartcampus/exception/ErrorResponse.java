package com.smartcampus.exception;

import java.time.LocalDateTime;

/**
 * Standard error response body returned by all exception mappers.
 * Provides a consistent JSON structure for error responses across the API.
 * 
 * Example JSON:
 * {
 *     "status": 409,
 *     "error": "Conflict",
 *     "message": "Cannot delete room because it has active sensors.",
 *     "timestamp": "2024-01-15T10:30:00"
 * }
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }

    // Getters and Setters

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
