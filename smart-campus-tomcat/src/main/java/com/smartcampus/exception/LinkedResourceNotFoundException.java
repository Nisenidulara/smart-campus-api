package com.smartcampus.exception;

/**
 * Custom exception thrown when a linked resource (e.g., roomId) referenced
 * in a request body does not exist in the system.
 * 
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 * 
 * HTTP 422 is preferred over 404 here because:
 * - The request URL itself is valid (e.g., POST /api/v1/sensors exists)
 * - The issue is with a field INSIDE the request body (roomId)
 * - 404 would imply the endpoint doesn't exist, which is misleading
 * - 422 indicates the server understood the request but cannot process it
 *   due to semantic validation errors in the payload
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
