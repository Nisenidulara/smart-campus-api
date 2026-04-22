package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorRoom;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * JAX-RS Resource class for managing sensors.
 * Handles CRUD operations on the /api/v1/sensors path.
 * 
 * Also provides a sub-resource locator for sensor readings
 * via the path /api/v1/sensors/{sensorId}/readings.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * Returns a list of all sensors.
     * Supports optional query parameter 'type' for filtering.
     * 
     * Example: GET /api/v1/sensors?type=CO2
     * 
     * We use @QueryParam for filtering because:
     * - Query parameters are the standard REST convention for filtering collections
     * - They are optional by nature, maintaining backward compatibility
     * - Clients can combine multiple filters without changing the URL structure
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors;

        if (type != null && !type.isEmpty()) {
            sensors = dataStore.getSensorsByType(type);
        } else {
            sensors = dataStore.getAllSensors();
        }

        return Response.ok(sensors).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns detailed metadata for a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensorById(sensorId);
        if (sensor == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * POST /api/v1/sensors
     * Creates a new sensor.
     * 
     * Validates that the roomId specified in the request body actually exists.
     * If the room does not exist, throws LinkedResourceNotFoundException (→ 422).
     * 
     * Uses @Consumes(APPLICATION_JSON) — if a client sends data in a different format
     * (e.g., text/plain, application/xml), JAX-RS will automatically return
     * HTTP 415 Unsupported Media Type before the method is even invoked.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Validate that the linked room exists
        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            throw new LinkedResourceNotFoundException(
                    "The 'roomId' field is required when creating a sensor."
            );
        }

        SensorRoom room = dataStore.getRoomById(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot create sensor: the specified roomId '" + sensor.getRoomId() +
                    "' does not exist in the system. Please provide a valid room ID."
            );
        }

        // Build via constructor (generates UUID + timestamp)
        Sensor newSensor = new Sensor(sensor.getRoomId(), sensor.getType(), sensor.getName());
        if (sensor.getStatus() != null) {
            newSensor.setStatus(sensor.getStatus());
        }
        if (sensor.getCurrentValue() != 0.0) {
            newSensor.setCurrentValue(sensor.getCurrentValue());
        }

        // Override with client-supplied ID if provided (e.g. "TEMP-001")
        if (sensor.getId() != null && !sensor.getId().trim().isEmpty()) {
            if (dataStore.getSensorById(sensor.getId()) != null) {
                ErrorResponse error = new ErrorResponse(409, "Conflict",
                        "A sensor with ID '" + sensor.getId() + "' already exists.");
                return Response.status(Response.Status.CONFLICT).entity(error).build();
            }
            newSensor.setId(sensor.getId());
        }

        dataStore.addSensor(newSensor);
        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

    /**
     * PUT /api/v1/sensors/{sensorId}
     * Updates an existing sensor.
     */
    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existingSensor = dataStore.getSensorById(sensorId);
        if (existingSensor == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        // Update fields while preserving ID and creation timestamp
        if (updatedSensor.getName() != null) {
            existingSensor.setName(updatedSensor.getName());
        }
        if (updatedSensor.getType() != null) {
            existingSensor.setType(updatedSensor.getType());
        }
        if (updatedSensor.getStatus() != null) {
            existingSensor.setStatus(updatedSensor.getStatus());
        }
        if (updatedSensor.getRoomId() != null) {
            // Validate new roomId if being changed
            SensorRoom room = dataStore.getRoomById(updatedSensor.getRoomId());
            if (room == null) {
                throw new LinkedResourceNotFoundException(
                        "Cannot update sensor: the specified roomId '" + updatedSensor.getRoomId() +
                        "' does not exist in the system."
                );
            }
            existingSensor.setRoomId(updatedSensor.getRoomId());
        }

        return Response.ok(existingSensor).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Deletes a sensor and its associated readings.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensorById(sensorId);
        if (sensor == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        dataStore.removeSensor(sensorId);
        return Response.ok()
                .entity("{\"message\": \"Sensor successfully deleted\", \"sensorId\": \"" + sensorId + "\"}")
                .build();
    }

    /**
     * Sub-Resource Locator for sensor readings.
     * 
     * Delegates all requests to /api/v1/sensors/{sensorId}/readings
     * to the SensorReadingResource class.
     * 
     * This pattern keeps the SensorResource focused on sensor-level operations
     * while SensorReadingResource handles all reading-specific logic.
     * It improves code organization, testability, and maintainability.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
