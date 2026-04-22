package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root discovery endpoint for the Smart Campus Sensor API.
 * 
 * Provides essential API metadata including versioning information,
 * administrative contact details, and a map of primary resource collections
 * following HATEOAS principles.
 */
@Path("/")
public class DiscoveryResource {

    /**
     * GET /api/v1
     * Returns a JSON object with API metadata and navigable resource links.
     * 
     * We use @Context UriInfo injected via method parameter to dynamically
     * determine the base URI. This ensures the API works correctly regardless
     * of the port (8080, 9090) or context path it's deployed on.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiDiscovery(@Context UriInfo uriInfo) {
        Map<String, Object> discovery = new LinkedHashMap<>();

        // API Metadata
        discovery.put("apiName", "Smart Campus Sensor API");
        discovery.put("version", "1.0");
        discovery.put("description", "RESTful API for managing campus sensor infrastructure including rooms, sensors, and sensor readings.");

        // Administrative Contact
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Smart Campus Admin");
        contact.put("email", "admin@smartcampus.edu");
        contact.put("department", "IT Infrastructure & IoT");
        discovery.put("contact", contact);

        // Resource Links (HATEOAS-style navigation)
        Map<String, Object> resources = new LinkedHashMap<>();

        // Dynamically get base URI from the request context
        String baseUri = uriInfo.getBaseUri().toString();
        
        // Remove trailing slash for cleaner URL building
        if (baseUri.endsWith("/")) {
            baseUri = baseUri.substring(0, baseUri.length() - 1);
        }

        Map<String, String> roomsLink = new LinkedHashMap<>();
        roomsLink.put("href", baseUri + "/rooms");
        roomsLink.put("method", "GET, POST");
        roomsLink.put("description", "Manage campus sensor rooms");
        resources.put("rooms", roomsLink);

        Map<String, String> sensorsLink = new LinkedHashMap<>();
        sensorsLink.put("href", baseUri + "/sensors");
        sensorsLink.put("method", "GET, POST");
        sensorsLink.put("description", "Manage sensors and their assignments");
        resources.put("sensors", sensorsLink);

        discovery.put("resources", resources);

        // Server Information
        Map<String, String> server = new LinkedHashMap<>();
        server.put("baseUri", baseUri);
        server.put("status", "running");
        server.put("deploymentMode", "Tomcat (WAR)");
        discovery.put("server", server);

        return Response.ok(discovery).build();
    }
}
