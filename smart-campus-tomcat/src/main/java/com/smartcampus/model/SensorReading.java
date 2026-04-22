package com.smartcampus.model;

import java.util.UUID;

/**
 * Represents a single reading taken by a sensor at a specific point in time.
 */
public class SensorReading {

    private String id;
    private String sensorId;
    private double value;
    private String unit;        // e.g., "°C", "ppm", "%"
    private long timestamp;     // epoch milliseconds

    // Default constructor required for JSON deserialization
    public SensorReading() {
    }

    public SensorReading(String sensorId, double value, String unit) {
        this.id = UUID.randomUUID().toString();
        this.sensorId = sensorId;
        this.value = value;
        this.unit = unit;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
