package com.smartcampus.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a physical room on campus that can contain sensors.
 */
public class SensorRoom {

    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds;
    private String location;
    private int floor;
    private String createdAt;

    // Default constructor required for JSON deserialization
    public SensorRoom() {
        this.sensorIds = new ArrayList<>();
    }

    public SensorRoom(String name, String location, int floor, int capacity) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.location = location;
        this.floor = floor;
        this.capacity = capacity;
        this.sensorIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now().toString();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }

    /**
     * Adds a sensor ID to this room's sensor list.
     */
    public void addSensorId(String sensorId) {
        if (this.sensorIds == null) {
            this.sensorIds = new ArrayList<>();
        }
        this.sensorIds.add(sensorId);
    }

    /**
     * Removes a sensor ID from this room's sensor list.
     */
    public void removeSensorId(String sensorId) {
        if (this.sensorIds != null) {
            this.sensorIds.remove(sensorId);
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
