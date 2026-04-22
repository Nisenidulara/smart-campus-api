# Smart Campus Sensor & Room Management API

A RESTful API built with **JAX-RS (Jersey)** and deployed on an **Apache Tomcat** servlet container for managing campus sensor infrastructure — rooms, sensors, and sensor readings. All data is stored in-memory using thread-safe `ConcurrentHashMap` data structures.

---

## Table of Contents

- [API Design Overview](#api-design-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [How to Build and Run](#how-to-build-and-run)
- [API Endpoints](#api-endpoints)
- [Sample curl Commands](#sample-curl-commands)
- [Error Handling](#error-handling)
- [Coursework Question Answers](#coursework-question-answers)

---

## API Design Overview

The Smart Campus Sensor API follows RESTful design principles with a versioned entry point at `/api/v1`. The API manages three core resources:

1. **Rooms** — Physical locations on campus (labs, lecture halls, server rooms)
2. **Sensors** — IoT devices installed in rooms (temperature, CO2, humidity sensors)
3. **Readings** — Historical data recorded by sensors (sub-resource of sensors)

Key design features:
- **HATEOAS Discovery Endpoint** at `GET /api/v1` for API navigation
- **Sub-Resource Locator Pattern** for sensor readings (`/sensors/{id}/readings`)
- **Custom Exception Mappers** for consistent JSON error responses (409, 422, 403, 500)
- **Request/Response Logging** via JAX-RS filters for API observability
- **Business Logic Constraints** — rooms with sensors cannot be deleted; maintenance sensors cannot accept readings

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| API Framework | JAX-RS 2.1 (Jersey 2.41) |
| Servlet Container | Apache Tomcat 9.x+ |
| JSON Processing | Jackson (via Jersey media) |
| Dependency Injection | HK2 |
| Build Tool | Apache Maven |
| Data Storage | `ConcurrentHashMap` (in-memory) |

---

## Project Structure

```
smart-campus-tomcat/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/smartcampus/
    │   ├── application/
    │   │   └── SmartCampusApplication.java   ← @ApplicationPath is handled in web.xml
    │   ├── model/
    │   │   ├── SensorRoom.java               ← Room model (id, name, capacity, location, floor, createdAt)
    │   │   ├── Sensor.java                   ← Sensor model (id, name, type, status, currentValue, roomId, createdAt)
    │   │   └── SensorReading.java            ← Reading model (id, sensorId, value, unit, timestamp)
    │   ├── repository/
    │   │   └── DataStore.java                ← Singleton ConcurrentHashMap data store
    │   ├── resource/
    │   │   ├── DiscoveryResource.java            ← GET /api/v1 (HATEOAS)
    │   │   ├── SensorRoomResource.java           ← GET/POST/PUT/DELETE /rooms
    │   │   ├── SensorResource.java               ← GET/POST/PUT/DELETE /sensors
    │   │   └── SensorReadingResource.java        ← Sub-resource for readings
    │   ├── exception/
    │   │   ├── ErrorResponse.java                ← Standard error response body
    │   │   ├── RoomNotEmptyException.java        ← 409 Conflict
    │   │   ├── LinkedResourceNotFoundException.java ← 422 Unprocessable Entity
    │   │   └── SensorUnavailableException.java   ← 403 Forbidden
    │   ├── mapper/
    │   │   ├── RoomNotEmptyExceptionMapper.java
    │   │   ├── LinkedResourceNotFoundExceptionMapper.java
    │   │   ├── SensorUnavailableExceptionMapper.java
    │   │   └── GenericExceptionMapper.java       ← 500 catch-all
    │   └── filter/
    │       └── LoggingFilter.java                ← Logs every request and response
    └── webapp/WEB-INF/
        └── web.xml                           ← Servlet container configuration
```

---

## How to Build and Run

### Prerequisites
- **Java 17** or later (`java -version` to verify)
- **Apache Maven 3.6+** (`mvn -version` to verify)

### Step 1: Clone the Repository
```bash
git clone https://github.com/Nisenidulara/smart-campus-api.git
cd smart-campus-api
```

### Step 2: Build the Project
```bash
mvn clean package
```
This will generate `smart-campus-api.war` in the `target/` directory.

### Step 3: Deploy to Tomcat
1. Ensure **Apache Tomcat** is running on port **9090**.
2. Copy the generated WAR file to the Tomcat `webapps/` directory:
   ```bash
   cp target/smart-campus-api.war /path/to/tomcat/webapps/
   ```
3. The API will be available at: **http://localhost:9090/smart-campus-api/api/v1**

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/smart-campus-api/api/v1` | Discovery endpoint (API metadata & HATEOAS links) |
| GET | `/smart-campus-api/api/v1/rooms` | List all rooms |
| POST | `/smart-campus-api/api/v1/rooms` | Create a new room |
| GET | `/smart-campus-api/api/v1/rooms/{roomId}` | Get room by ID |
| PUT | `/smart-campus-api/api/v1/rooms/{roomId}` | Update a room |
| DELETE | `/smart-campus-api/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors exist) |
| GET | `/smart-campus-api/api/v1/sensors` | List all sensors (supports `?type=` filter) |
| POST | `/smart-campus-api/api/v1/sensors` | Create a sensor (validates roomId exists) |
| GET | `/smart-campus-api/api/v1/sensors/{sensorId}` | Get sensor by ID |
| PUT | `/smart-campus-api/api/v1/sensors/{sensorId}` | Update a sensor |
| DELETE | `/smart-campus-api/api/v1/sensors/{sensorId}` | Delete a sensor |
| GET | `/smart-campus-api/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/smart-campus-api/api/v1/sensors/{sensorId}/readings` | Add a reading (blocked if MAINTENANCE or OFFLINE) |

---

## Sample curl Commands


### Step 1 — Get API Info
```bash
curl -X GET http://localhost:9090/smart-campus-api/api/v1
```

### Step 2 — List All Rooms (copy a roomId from the response)
```bash
curl -X GET http://localhost:9090/smart-campus-api/api/v1/rooms
```

### Step 3 — Create a New Room (note the id in the response)
```bash
curl -X POST http://localhost:9090/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"Physics Lab 202","location":"Building 4, Wing B","floor":2,"capacity":40}'
```

### Step 4 — List All Sensors (copy an ACTIVE sensorId and a MAINTENANCE sensorId)
```bash
curl -X GET http://localhost:9090/smart-campus-api/api/v1/sensors
```

### Step 5 — Filter Sensors by Type
```bash
curl -X GET "http://localhost:9090/smart-campus-api/api/v1/sensors?type=CO2"
```

### Step 6 — Create a New Sensor (replace ROOM_ID with a real id from Step 2)
```bash
curl -X POST http://localhost:9090/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"name":"New Temp Sensor","type":"Temperature","roomId":"PASTE_ROOM_ID_HERE"}'
```

### Step 7 — Get Sensor Readings (replace SENSOR_ID with an active sensor id from Step 4)
```bash
curl -X GET http://localhost:9090/smart-campus-api/api/v1/sensors/PASTE_ACTIVE_SENSOR_ID/readings
```

### Step 8 — Post a New Reading (replace SENSOR_ID with an active sensor id)
```bash
curl -X POST http://localhost:9090/smart-campus-api/api/v1/sensors/PASTE_ACTIVE_SENSOR_ID/readings \
  -H "Content-Type: application/json" \
  -d '{"value":25.5,"unit":"C"}'
```

### Step 9 — Try to Delete a Room WITH Sensors (triggers 409 Conflict)
```bash
curl -X DELETE http://localhost:9090/smart-campus-api/api/v1/rooms/PASTE_ROOM_WITH_SENSORS_ID
```

### Step 10 — Delete the New Room WITHOUT Sensors (triggers 200 OK)
```bash
curl -X DELETE http://localhost:9090/smart-campus-api/api/v1/rooms/PASTE_NEW_ROOM_ID_FROM_STEP3
```

### Step 11 — Create Sensor with Invalid roomId (triggers 422)
```bash
curl -X POST http://localhost:9090/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"name":"Ghost Sensor","type":"CO2","roomId":"fake-room-that-does-not-exist"}'
```

### Step 12 — Post Reading to MAINTENANCE Sensor (triggers 403)
```bash
curl -X POST http://localhost:9090/smart-campus-api/api/v1/sensors/PASTE_MAINTENANCE_SENSOR_ID/readings \
  -H "Content-Type: application/json" \
  -d '{"value":10.0,"unit":"C"}'
```

---

## Error Handling

The API uses custom Exception Mappers for consistent JSON error responses:

| HTTP Status | Exception | Scenario |
|---|---|---|
| **409 Conflict** | `RoomNotEmptyException` | Deleting a room that still has sensors assigned |
| **422 Unprocessable Entity** | `LinkedResourceNotFoundException` | Creating a sensor with a roomId that does not exist |
| **403 Forbidden** | `SensorUnavailableException` | Posting a reading to a sensor in MAINTENANCE or OFFLINE status |
| **500 Internal Server Error** | `GenericExceptionMapper` | Any unexpected runtime error |

All error responses follow this format:
```json
{
    "status": 409,
    "error": "Conflict",
    "message": "Cannot delete room because it still has active sensors assigned.",
    "timestamp": "2026-04-22T10:30:00"
}
```

---

## Coursework Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS uses a **per-request lifecycle** for resource classes. This means a **new instance** of the resource class is instantiated for every incoming HTTP request. The container creates the object, processes the request, and then the instance is eligible for garbage collection.

This architectural decision has significant implications for managing in-memory data structures:

1. **Shared State Problem**: Since each request gets a fresh resource instance, any data stored as instance fields would be lost between requests. If a `SensorRoomResource` stored rooms in a local `HashMap`, each new request would start with an empty map.

2. **Solution — Centralized Data Store**: To persist data across requests, we use a **singleton `DataStore` class**. All resource instances access the same `DataStore.getInstance()`, ensuring data consistency.

3. **Thread Safety**: Because multiple requests can execute concurrently, sharing the same data store, we must use thread-safe data structures. We use `ConcurrentHashMap` instead of `HashMap` to prevent race conditions such as lost updates and `ConcurrentModificationException`.

4. **Alternative — Singleton Scope**: JAX-RS supports `@Singleton` annotation on resource classes, creating one shared instance. However, per-request is the default and preferred approach since it avoids shared mutable state within the resource class itself.

---

### Part 1.2 — HATEOAS

HATEOAS (Hypermedia As The Engine Of Application State) is considered a hallmark of advanced RESTful design because it makes APIs **self-descriptive and navigable**:

1. **Dynamic Discovery**: Clients do not need to hardcode URLs. The API provides links to related resources. Our Discovery endpoint at `GET /api/v1` returns a map of available resources and their paths.

2. **Reduced Coupling**: Client applications are decoupled from fixed URL structures. If the server changes a resource path, the client adapts automatically by following updated links.

3. **Self-Documentation**: New developers can explore the API by calling the root endpoint and following links, reducing dependency on external documentation which can become stale.

4. **Compared to Static Documentation**: Static docs must be manually kept in sync with code. HATEOAS links are generated at runtime and are always accurate and up-to-date.

---

### Part 2.1 — ID-only vs Full Object in List Responses

**Returning Full Objects:**
- **Pros**: Clients receive all data in a single request — no additional network calls needed. Ideal for UIs displaying room details in a list view.
- **Cons**: Higher bandwidth usage, especially with many fields or large lists.

**Returning Only IDs:**
- **Pros**: Minimal bandwidth usage, very fast responses.
- **Cons**: Requires N additional `GET /rooms/{id}` requests to fetch details — the "N+1 query problem" — increasing latency and server load.

**Our Implementation**: We return full room objects because the dataset fits in memory and payloads are small. For large-scale production APIs, pagination with `?page=1&size=20` and sparse fieldsets (`?fields=id,name`) would offer a balanced approach.

---

### Part 2.2 — Is DELETE Idempotent?

Yes, DELETE is **effectively idempotent** in our implementation. Idempotency means making the same request multiple times produces the same server state as making it once.

- **First Request**: The room exists, is deleted, server returns **200 OK**. Server state changes — room is removed.
- **Second Request**: Room no longer exists, server returns **404 Not Found**. Server state does NOT change — room remains absent.

The server state after the first call is identical to the state after any subsequent call — the room remains deleted. While response codes differ (200 vs 404), idempotency focuses on server-side state, not response codes. This makes DELETE safe to retry in case of network failures.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

When we annotate a method with `@Consumes(MediaType.APPLICATION_JSON)`, we tell JAX-RS this endpoint **only** accepts `application/json` content.

If a client sends data with a different `Content-Type` header (e.g., `text/plain` or `application/xml`), JAX-RS handles this **automatically** before the method is invoked:

1. The runtime checks the `Content-Type` header of the incoming request.
2. It compares it against the `@Consumes` annotation on the matched method.
3. If no matching media type is found, JAX-RS returns **HTTP 415 Unsupported Media Type** without executing the resource method.

This is content negotiation built into JAX-RS — no manual validation code is needed.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

We implemented filtering using `@QueryParam("type")` (e.g., `GET /sensors?type=CO2`) instead of path-based filtering (e.g., `GET /sensors/type/CO2`). Query parameters are superior for filtering because:

1. **Optional by Nature**: `GET /sensors` returns all sensors; `GET /sensors?type=CO2` filters them. Path-based design would need separate route handlers.

2. **Combinable Filters**: Query parameters support multiple filters naturally: `GET /sensors?type=CO2&status=ACTIVE`. Path-based filtering becomes unwieldy with multiple criteria.

3. **RESTful Semantics**: The URL path identifies a **specific resource**. `/sensors` identifies the sensors collection. Adding `/type/CO2` implies a different resource, which is misleading.

4. **Convention**: Industry-standard APIs (GitHub, Stripe, Google) all use query parameters for filtering.

---

### Part 4.1 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern offers several key architectural benefits:

1. **Separation of Concerns**: Each resource class handles a single level of the hierarchy. `SensorResource` manages sensors; `SensorReadingResource` manages readings.

2. **Reduced Complexity**: Without sub-resources, all paths would be in one massive controller — hard to read and maintain.

3. **Testability**: Smaller, focused classes are much easier to unit test independently.

4. **Context Passing**: The locator method validates the sensor exists and passes `sensorId` to `SensorReadingResource` cleanly.

5. **Team Scalability**: Different developers can work on different sub-resource classes without merge conflicts.

In our implementation, `SensorResource` has a method annotated with `@Path("{sensorId}/readings")` that returns a new `SensorReadingResource(sensorId)` instance. JAX-RS dispatches the remaining path and HTTP method to the appropriate method within `SensorReadingResource`.

---

### Part 4.2 — Historical Data Management and Side Effects

The `SensorReadingResource` class handles two operations for the `/sensors/{sensorId}/readings` path:

**GET /** — Returns the complete reading history for the sensor. All readings are stored in the `DataStore` as a `List<SensorReading>` keyed by `sensorId`. The response includes the sensor ID, total count, and the full list of readings with timestamps, values, and units.

**POST /** — Appends a new reading to the sensor's history. When a new reading is successfully posted:

1. The reading is assigned a unique UUID and the current epoch timestamp automatically.
2. It is added to the sensor's reading list in the `DataStore`.
3. **Side Effect**: The parent `Sensor` object's `currentValue` field is immediately updated to reflect the new reading value. This ensures data consistency across the API — any client that subsequently calls `GET /sensors/{sensorId}` will see the updated `currentValue` matching the latest reading, keeping both resources in sync without requiring a separate update call.

---

### Part 5.1 — Room Conflict (409)

When a client attempts to DELETE a room that still has sensors assigned, the API throws a `RoomNotEmptyException`. The `RoomNotEmptyExceptionMapper` intercepts this and returns **HTTP 409 Conflict** with a structured JSON error body explaining that the room cannot be decommissioned while it still has active hardware. This prevents orphaned sensor data — sensors would have a `roomId` pointing to a deleted room, causing data integrity issues.

---

### Part 5.2 — Why HTTP 422 Over 404

HTTP 422 (Unprocessable Entity) is more semantically accurate than 404 (Not Found) for these reasons:

1. **404 means the URL is wrong**: HTTP 404 indicates the **requested endpoint URL** was not found. When a client sends `POST /api/v1/sensors` with an invalid `roomId`, the endpoint `/sensors` absolutely exists — returning 404 would mislead the client.

2. **422 means the data is semantically invalid**: HTTP 422 indicates the server understood the request and the JSON syntax is valid, but the request cannot be processed due to **semantic validation errors** in the payload. The `roomId` references a non-existent entity — a business logic validation failure.

3. **Clarity**: Receiving 422 tells the client "your request format is correct, but the data inside it references something that doesn't exist," helping developers debug faster.

---

### Part 5.3 — State Constraint (403 Forbidden)

When a sensor is marked as `MAINTENANCE` or `OFFLINE`, it is physically disconnected from the campus network and cannot capture or transmit new data. Attempting to POST a reading to such a sensor throws a `SensorUnavailableException`, which is mapped to **HTTP 403 Forbidden**.

HTTP 403 is the correct status code here because:
- The request is **syntactically valid** — the sensor exists and the JSON payload is correct.
- The server **understands** the request but **refuses** to process it due to the current state of the sensor.
- 403 communicates "you are not permitted to perform this action right now" — which accurately reflects that the sensor's current status prohibits accepting new readings.
- It is more precise than 400 (Bad Request), which implies a client error in the request format rather than a state constraint.

---

### Part 5.4 — Cybersecurity Risks of Exposing Stack Traces

Exposing internal Java stack traces to external consumers poses significant security risks:

1. **Technology Fingerprinting**: Stack traces reveal exact frameworks and versions (e.g., `jersey-server-2.41.jar`). Attackers can search for known CVEs targeting those specific versions.

2. **Internal Architecture Exposure**: Package names and class names reveal the internal structure, making it easier to identify attack vectors.

3. **File System Path Disclosure**: Stack traces include absolute file paths, revealing the OS, deployment paths, and directory structure.

4. **Business Logic Clues**: Method names reveal business logic flow, helping attackers craft targeted exploits.

5. **Database Information**: SQL exception traces may expose table names, column names, or connection strings.

Our `GenericExceptionMapper` catch-all ensures no internal details reach the client. The full exception is logged internally for debugging, while a generic safe message is returned to the consumer.

---

### Part 5.5 — Why Filters for Logging vs Manual Logger Calls

Using JAX-RS Filters for logging is advantageous over manual `Logger.info()` statements for several reasons:

1. **Automatic Coverage**: Filters apply to every request and response automatically — no risk of missing an endpoint.

2. **Separation of Concerns**: Resource methods should focus on business logic. Logging is a cross-cutting concern that belongs in filters.

3. **Single Point of Change**: Changing the log format requires editing one filter class, not every resource method.

4. **Consistency**: A filter guarantees uniform log format across all endpoints.

5. **DRY Principle**: One filter replaces dozens of repeated `Logger.info()` calls.

Our `LoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter`, logging the HTTP method and URI for incoming requests and the status code for outgoing responses using `java.util.logging.Logger`.
