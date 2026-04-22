package com.smartcampus.exception;

/**
 * Custom exception thrown when attempting to delete a room that still
 * has active sensors assigned to it.
 * 
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
