package com.smartcampus.mapper;

import com.smartcampus.exception.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "catch-all" Exception Mapper for any unexpected runtime errors.
 * 
 * This mapper intercepts any unhandled exceptions (e.g., NullPointerException,
 * IndexOutOfBoundsException, IllegalArgumentException) and returns a generic
 * HTTP 500 Internal Server Error response.
 * 
 * Security Consideration:
 * Exposing raw Java stack traces to external API consumers is a significant
 * security risk. Stack traces can reveal:
 * - Internal package structures and class names
 * - Framework versions and dependencies
 * - Database connection strings or configurations
 * - File system paths on the server
 * - Business logic flow and potential vulnerabilities
 * 
 * An attacker could use this information to:
 * - Identify known vulnerabilities in specific framework versions
 * - Understand the internal architecture to plan targeted attacks
 * - Discover potential injection points or unprotected endpoints
 * 
 * This mapper ensures no internal details leak to the client.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full exception internally for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by GenericExceptionMapper", exception);

        // Return a generic error message — never expose internal details
        ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator if the problem persists."
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
