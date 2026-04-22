package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Sub-Resource class for managing sensor readings.
 * 
 * This class is NOT annotated with @Path because it is instantiated
 * via a sub-resource locator method in SensorResource.
 * All paths are relative to /api/v1/sensors/{sensorId}/readings.
 * 
 * The Sub-Resource Locator pattern delegates responsibility to a dedicated class,
 * which helps manage complexity in large APIs by separating concerns.
 * Each resource class focuses on a single level of the resource hierarchy.
 */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * Constructor called by the sub-resource locator in SensorResource.
     * 
     * @param sensorId the ID of the parent sensor
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the complete history of readings for this sensor.
     */
    @GET
    public Response getAllReadings() {
        // Validate sensor exists
        Sensor sensor = dataStore.getSensorById(sensorId);
        if (sensor == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        List<SensorReading> readings = dataStore.getReadingsBySensorId(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings/{readingId}
     * Returns a specific reading by its ID.
     */
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        // Validate sensor exists
        Sensor sensor = dataStore.getSensorById(sensorId);
        if (sensor == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        List<SensorReading> readings = dataStore.getReadingsBySensorId(sensorId);
        for (SensorReading reading : readings) {
            if (reading.getId().equals(readingId)) {
                return Response.ok(reading).build();
            }
        }

        ErrorResponse error = new ErrorResponse(404, "Not Found",
                "Reading with ID '" + readingId + "' was not found for sensor '" + sensorId + "'.");
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading for this sensor.
     * 
     * Business Logic:
     * - If the sensor status is "MAINTENANCE", the sensor is physically disconnected
     *   and cannot accept new readings → throws SensorUnavailableException (→ 403).
     * 
     * Side Effect:
     * - A successful POST triggers an update to the currentValue field on the
     *   parent Sensor object to ensure data consistency across the API.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // Validate sensor exists
        Sensor sensor = dataStore.getSensorById(sensorId);
        if (sensor == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Sensor with ID '" + sensorId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        // Check if sensor is unavailable (MAINTENANCE or OFFLINE)
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensor.getName() + "' (ID: " + sensorId +
                    ") is currently in MAINTENANCE mode and cannot accept new readings. " +
                    "Please wait until the sensor is back online."
            );
        }
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensor.getName() + "' (ID: " + sensorId +
                    ") is currently OFFLINE and cannot accept new readings."
            );
        }

        // Create the reading with auto-generated ID and timestamp
        SensorReading newReading = new SensorReading(sensorId, reading.getValue(), reading.getUnit());
        dataStore.addReading(sensorId, newReading);

        // Side Effect: Update the currentValue on the parent sensor
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}
