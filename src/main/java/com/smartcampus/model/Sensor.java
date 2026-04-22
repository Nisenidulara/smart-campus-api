package com.smartcampus.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a sensor device installed in a room on campus.
 * Each sensor is linked to a specific room via roomId.
 */
public class Sensor {

    private String id;
    private String roomId;
    private String type;       // e.g., "Temperature", "CO2", "Humidity"
    private String name;
    private String status;     // ACTIVE, MAINTENANCE, OFFLINE
    private double currentValue;
    private String createdAt;

    // Default constructor required for JSON deserialization
    public Sensor() {
    }

    public Sensor(String roomId, String type, String name) {
        this.id = UUID.randomUUID().toString();
        this.roomId = roomId;
        this.type = type;
        this.name = name;
        this.status = "ACTIVE";
        this.currentValue = 0.0;
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
