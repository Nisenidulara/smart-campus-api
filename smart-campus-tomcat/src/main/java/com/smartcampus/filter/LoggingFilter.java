package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS Filter for API observability.
 * 
 * Implements both ContainerRequestFilter and ContainerResponseFilter to log:
 * - Incoming requests: HTTP method and URI
 * - Outgoing responses: HTTP status code
 * 
 * Uses java.util.logging.Logger as required by the specification.
 * 
 * Why use JAX-RS Filters instead of manual Logger.info() in each method?
 * - Filters apply globally to ALL endpoints automatically (cross-cutting concern)
 * - No risk of forgetting to add logging to a new endpoint
 * - Single point of change for logging format/behavior
 * - Follows the Separation of Concerns principle — resource methods focus on business logic
 * - Can be easily enabled/disabled without modifying any resource class
 * - Consistent logging format across the entire API
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Filters incoming requests.
     * Logs the HTTP method and request URI for every incoming request.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();

        LOGGER.info("=> REQUEST:  " + method + " " + uri);
    }

    /**
     * Filters outgoing responses.
     * Logs the HTTP status code for every outgoing response.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        int status = responseContext.getStatus();

        LOGGER.info("<= RESPONSE: " + method + " " + uri + " -> " + status);
    }
}
