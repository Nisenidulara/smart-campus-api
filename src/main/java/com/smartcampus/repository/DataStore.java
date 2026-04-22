package com.smartcampus.repository;

import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.model.SensorRoom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe singleton in-memory data store for the Smart Campus API.
 * Uses ConcurrentHashMap for thread safety in a per-request JAX-RS lifecycle.
 * 
 * This class is implemented as a singleton to ensure all resource class instances
 * share the same data. Since JAX-RS creates a new resource instance per request,
 * we need a centralized, thread-safe store for our data.
 */
public class DataStore {

    // Singleton instance
    private static DataStore instance;

    // Thread-safe maps keyed by entity ID
    private final Map<String, SensorRoom> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();  // keyed by sensorId

    // Private constructor to enforce singleton pattern
    private DataStore() {
        initializeSampleData();
    }

    /**
     * Returns the singleton instance of the DataStore.
     * Uses double-checked locking for thread-safe lazy initialization.
     */
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    /**
     * Pre-seeds the data store with sample data for demonstration purposes.
     */
    private void initializeSampleData() {
        // Create sample rooms with capacity
        SensorRoom room1 = new SensorRoom("Lab A - Computer Science", "Building 1, Wing A", 2, 30);
        SensorRoom room2 = new SensorRoom("Lecture Hall 101", "Building 2, Main Block", 1, 120);
        SensorRoom room3 = new SensorRoom("Server Room B3", "Building 3, Basement", 0, 10);

        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);
        rooms.put(room3.getId(), room3);

        // Create sample sensors linked to rooms
        Sensor sensor1 = new Sensor(room1.getId(), "Temperature", "Temp Sensor A1");
        Sensor sensor2 = new Sensor(room1.getId(), "CO2", "CO2 Monitor A1");
        Sensor sensor3 = new Sensor(room2.getId(), "Humidity", "Humidity Sensor LH101");
        Sensor sensor4 = new Sensor(room3.getId(), "Temperature", "Temp Sensor B3");
        sensor4.setStatus("MAINTENANCE");  // One sensor in maintenance for testing

        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);
        sensors.put(sensor3.getId(), sensor3);
        sensors.put(sensor4.getId(), sensor4);

        // Maintain sensorIds on parent rooms
        room1.addSensorId(sensor1.getId());
        room1.addSensorId(sensor2.getId());
        room2.addSensorId(sensor3.getId());
        room3.addSensorId(sensor4.getId());

        // Initialize thread-safe reading lists for each sensor
        readings.put(sensor1.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(sensor2.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(sensor3.getId(), Collections.synchronizedList(new ArrayList<>()));
        readings.put(sensor4.getId(), Collections.synchronizedList(new ArrayList<>()));

        // Add a sample reading to sensor1
        SensorReading reading1 = new SensorReading(sensor1.getId(), 23.5, "°C");
        readings.get(sensor1.getId()).add(reading1);
        sensor1.setCurrentValue(23.5);
    }

    // =========================================================================
    // Room Operations
    // =========================================================================

    public Map<String, SensorRoom> getRooms() {
        return rooms;
    }

    public List<SensorRoom> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public SensorRoom getRoomById(String id) {
        return rooms.get(id);
    }

    public void addRoom(SensorRoom room) {
        rooms.put(room.getId(), room);
    }

    public SensorRoom removeRoom(String id) {
        return rooms.remove(id);
    }

    // =========================================================================
    // Sensor Operations
    // =========================================================================

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public List<Sensor> getAllSensors() {
        return new ArrayList<>(sensors.values());
    }

    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    /**
     * Adds a sensor and updates the parent room's sensorIds list.
     */
    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Initialize a thread-safe empty reading list for the new sensor
        readings.putIfAbsent(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));
        // Update parent room's sensorIds list
        SensorRoom room = rooms.get(sensor.getRoomId());
        if (room != null) {
            room.addSensorId(sensor.getId());
        }
    }

    /**
     * Removes a sensor and updates the parent room's sensorIds list.
     */
    public Sensor removeSensor(String id) {
        Sensor sensor = sensors.get(id);
        if (sensor != null) {
            // Remove from parent room's sensorIds list
            SensorRoom room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.removeSensorId(id);
            }
        }
        readings.remove(id);  // Also remove associated readings
        return sensors.remove(id);
    }

    /**
     * Returns all sensors assigned to a specific room.
     */
    public List<Sensor> getSensorsByRoomId(String roomId) {
        return sensors.values().stream()
                .filter(s -> s.getRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    /**
     * Returns all sensors matching a specific type (case-insensitive).
     */
    public List<Sensor> getSensorsByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Reading Operations
    // =========================================================================

    public List<SensorReading> getReadingsBySensorId(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>())).add(reading);
    }
}
