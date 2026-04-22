package com.smartcampus.mapper;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception Mapper for LinkedResourceNotFoundException.
 * 
 * Maps to HTTP 422 Unprocessable Entity when a client submits a valid JSON payload
 * that references a non-existent linked resource (e.g., a sensor with an invalid roomId).
 * 
 * HTTP 422 is more semantically accurate than 404 in this case because:
 * - The request endpoint itself exists and is valid
 * - The JSON payload is syntactically correct
 * - The issue is a semantic validation failure: a referenced entity doesn't exist
 * - 404 would imply the URL/endpoint is wrong, which is misleading
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                422,
                "Unprocessable Entity",
                exception.getMessage()
        );

        return Response.status(422)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
