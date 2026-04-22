package com.smartcampus.exception;

/**
 * Custom exception thrown when attempting to post a reading to a sensor
 * that is currently in MAINTENANCE mode and cannot accept data.
 * 
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
