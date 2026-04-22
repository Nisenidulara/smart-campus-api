package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorRoom;
import com.smartcampus.repository.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * JAX-RS Resource class for managing sensor rooms.
 * Handles CRUD operations on the /api/v1/rooms path.
 *
 * JAX-RS Lifecycle: By default, a new instance of this class is created
 * for every incoming HTTP request (per-request lifecycle). This is why
 * we use the DataStore singleton for shared state.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a list of all sensor rooms.
     */
    @GET
    public Response getAllRooms() {
        List<SensorRoom> rooms = dataStore.getAllRooms();
        return Response.ok(rooms).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        SensorRoom room = dataStore.getRoomById(roomId);
        if (room == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Room with ID '" + roomId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new sensor room.
     *
     * If the client provides an "id" field (e.g., "LIB-301"), that ID is used.
     * Otherwise a UUID is auto-generated.
     * Returns HTTP 201 Created with the new room object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(SensorRoom room) {
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            ErrorResponse error = new ErrorResponse(400, "Bad Request",
                    "The 'name' field is required when creating a room.");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // Build via constructor (generates UUID + timestamp)
        SensorRoom newRoom = new SensorRoom(
                room.getName(),
                room.getLocation(),
                room.getFloor(),
                room.getCapacity()
        );

        // Override with client-supplied ID if provided (e.g. "LIB-301")
        if (room.getId() != null && !room.getId().trim().isEmpty()) {
            if (dataStore.getRoomById(room.getId()) != null) {
                ErrorResponse error = new ErrorResponse(409, "Conflict",
                        "A room with ID '" + room.getId() + "' already exists.");
                return Response.status(Response.Status.CONFLICT).entity(error).build();
            }
            newRoom.setId(room.getId());
        }

        dataStore.addRoom(newRoom);
        return Response.status(Response.Status.CREATED).entity(newRoom).build();
    }

    /**
     * PUT /api/v1/rooms/{roomId}
     * Updates an existing sensor room.
     */
    @PUT
    @Path("/{roomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRoom(@PathParam("roomId") String roomId, SensorRoom updatedRoom) {
        SensorRoom existingRoom = dataStore.getRoomById(roomId);
        if (existingRoom == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Room with ID '" + roomId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        // Update fields while preserving ID, creation timestamp, and sensorIds
        existingRoom.setName(updatedRoom.getName());
        existingRoom.setLocation(updatedRoom.getLocation());
        existingRoom.setFloor(updatedRoom.getFloor());
        existingRoom.setCapacity(updatedRoom.getCapacity());

        return Response.ok(existingRoom).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a sensor room.
     *
     * Business Logic Constraint: A room cannot be deleted if it still has
     * active sensors assigned to it. This prevents orphaned sensor data.
     *
     * Idempotency: The first DELETE removes the room and returns 200.
     * Subsequent DELETE requests for the same ID return 404 (room not found).
     * This is technically idempotent as the server state does not change
     * after the first successful deletion.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        SensorRoom room = dataStore.getRoomById(roomId);
        if (room == null) {
            ErrorResponse error = new ErrorResponse(404, "Not Found",
                    "Room with ID '" + roomId + "' was not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        }

        // Check if room still has sensors assigned
        List<Sensor> assignedSensors = dataStore.getSensorsByRoomId(roomId);
        if (!assignedSensors.isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + room.getName() + "' (ID: " + roomId +
                    ") because it still has " + assignedSensors.size() +
                    " active sensor(s) assigned to it. Please remove or reassign all sensors before deleting this room."
            );
        }

        dataStore.removeRoom(roomId);
        return Response.ok()
                .entity("{\"message\": \"Room successfully deleted\", \"roomId\": \"" + roomId + "\"}")
                .build();
    }
}
