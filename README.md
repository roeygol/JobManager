# Job Orchestrator Service

A production-grade Java 21 Spring Boot microservice that orchestrates job execution across downstream microservices. This service acts as a request proxy, dispatcher, and job lifecycle manager - conceptually similar to Postman as a backend service.

## Overview

The Job Orchestrator Service accepts incoming HTTP requests, resolves target microservices by job name, forwards the entire HTTP request as-is, executes it asynchronously, and tracks its lifecycle. The service is not a business executor - it is a request proxy, dispatcher, and job lifecycle manager.

## Architecture

The service follows a clean three-layer architecture:

- **API Layer (Controller)**: REST controllers with DTOs, validation, and exception handling
- **Application/Domain Layer (Service)**: Business logic, orchestration, idempotency, and async execution management
- **Persistence Layer (DAO)**: JPA repositories with CrudRepository only

### Key Components

- **JobController**: REST API endpoints for job operations
- **JobOrchestrationService**: Core orchestration logic with idempotency and cancellation support
- **HttpForwardingService**: HTTP request forwarding to target microservices
- **JobMappingService**: Job name to service mapping resolution
- **JobStatusRepository**: Persistence for job execution state
- **JobRestMappingRepository**: Persistence for routing configuration

## Features

### Core Features
- ✅ **HTTP Request Forwarding**: Forwards any HTTP method (GET, POST, PUT, DELETE, PATCH) with headers, query params, and body
- ✅ **Asynchronous Execution**: Managed thread pool execution with `@Async` and `ExecutorService`
- ✅ **Idempotent Job Creation**: Prevents duplicate job creation using `Idempotency-Key` header
- ✅ **Job Cancellation**: Cancel in-flight jobs with thread-safe Future registry
- ✅ **Job Lifecycle Tracking**: Complete execution state management (STARTED, IN_PROGRESS, SUCCESS, FAILED, CANCELLED)
- ✅ **Request/Response Preservation**: Maintains original request structure and captures response details

### Technical Features
- ✅ **Thread-Safe Execution Registry**: ConcurrentHashMap for active job tracking
- ✅ **Cooperative Cancellation**: Uses `Future.cancel(true)` for thread interruption
- ✅ **DTO-Based API**: No entity leakage to API layer
- ✅ **Clean Transactional Boundaries**: Proper `@Transactional` demarcation
- ✅ **Comprehensive Validation**: Jakarta Bean Validation on all inputs
- ✅ **Structured Error Handling**: Domain-specific exceptions with proper HTTP status codes
- ✅ **Structured Logging**: SLF4J with clear state transitions
- ✅ **OpenAPI/Swagger Documentation**: Auto-generated API documentation
- ✅ **Comprehensive Testing**: Unit tests and integration tests

## API Endpoints

### POST /job/create/{jobName}

Creates and executes a job by forwarding the entire incoming HTTP request.

**Path Parameters:**
- `jobName` (required): Job name to resolve destination service

**Headers:**
- `Idempotency-Key` (required): Unique key for idempotent requests

**Request Body:** Any HTTP request body (JSON, XML, etc.)

**Query Parameters:** Any query parameters from the original request

**Response:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/job/create/data-processing" \
  -H "Idempotency-Key: unique-request-id-123" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer token123" \
  -d '{"key": "value"}' \
  -G -d "param1=value1" -d "param2=value2"
```

**Status Codes:**
- `200 OK`: Job created successfully (or existing job returned for idempotent request)
- `400 BAD_REQUEST`: Missing Idempotency-Key header, invalid job name, or job mapping not found
- `404 NOT_FOUND`: Job mapping not found

### GET /job/{uuid}

Retrieves the current execution status of a job.

**Path Parameters:**
- `uuid` (required): Job execution UUID

**Response:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "response": "Remote service response body",
  "httpStatus": 200,
  "startDate": "2024-01-01T10:00:00",
  "endDate": "2024-01-01T10:00:15"
}
```

**Status Values:**
- `STARTED`: Job created, execution not yet started
- `IN_PROGRESS`: Job execution in progress
- `SUCCESS`: Job completed successfully
- `FAILED`: Job execution failed
- `CANCELLED`: Job execution was cancelled

**Example Request:**
```bash
curl -X GET "http://localhost:8080/job/550e8400-e29b-41d4-a716-446655440000"
```

**Status Codes:**
- `200 OK`: Job status retrieved successfully
- `400 BAD_REQUEST`: Invalid UUID format
- `404 NOT_FOUND`: Job not found

### POST /job/cancel/{uuid}

Cancels an in-flight job execution.

**Path Parameters:**
- `uuid` (required): Job execution UUID

**Response:**
```json
{
  "message": "Job cancellation request processed",
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/job/cancel/550e8400-e29b-41d4-a716-446655440000"
```

**Behavior:**
- If job is already completed (SUCCESS/FAILED/CANCELLED) → No-op, returns 200
- If job is running (IN_PROGRESS) → Cancels the executing thread and updates status to CANCELLED
- If job does not exist → Returns 404

**Status Codes:**
- `200 OK`: Cancellation request processed
- `400 BAD_REQUEST`: Invalid UUID format
- `404 NOT_FOUND`: Job not found

## Idempotency

The service supports idempotent job creation using the `Idempotency-Key` header:

1. **Client provides `Idempotency-Key` header** with each job creation request
2. **Service checks for existing job** with the same idempotency key
3. **If found**: Returns existing job UUID without creating a new execution
4. **If not found**: Creates new job and stores the idempotency key

**Important:** The same `jobName` + `Idempotency-Key` combination will always return the same UUID.

**Example:**
```bash
# First request
curl -X POST "http://localhost:8080/job/create/my-job" \
  -H "Idempotency-Key: key-123"

# Response: {"uuid": "abc-123"}

# Second request with same key (idempotent)
curl -X POST "http://localhost:8080/job/create/my-job" \
  -H "Idempotency-Key: key-123"

# Response: {"uuid": "abc-123"} (same UUID, no new job created)
```

## Job Execution Flow

1. **Client sends POST /job/create/{jobName}** with:
   - `Idempotency-Key` header (required)
   - Original HTTP method, headers, query params, and body

2. **Service validates inputs**:
   - Job name format and length
   - Idempotency key presence and format

3. **Idempotency check**:
   - If existing job found → return existing UUID
   - If not found → proceed with creation

4. **Job mapping resolution**:
   - Resolve `jobName` → target service URL/port
   - Validate mapping exists

5. **JobStatus entity created**:
   - Generate UUID
   - Set status to STARTED
   - Store idempotency key
   - Persist to database

6. **UUID returned immediately** to client

7. **Asynchronous execution** (in background thread):
   - Status updated to IN_PROGRESS
   - HTTP request forwarded to target service (preserving method, headers, query params, body)
   - Response captured (body and HTTP status)
   - Status updated to SUCCESS or FAILED
   - Response and HTTP status persisted

8. **Client polls GET /job/{uuid}** for status updates

## Cancellation Flow

1. **Client sends POST /job/cancel/{uuid}**

2. **Service validates**:
   - UUID format
   - Job exists

3. **Cancellation logic**:
   - If job already completed → No-op
   - If job is running:
     - Cancel Future from execution registry
     - Update status to CANCELLED
     - Set end date
     - Remove from registry

4. **Response returned** to client

## Database Schema

### H2 Database (JPA Entities)

#### JobRestMapping
Defines routing configuration for jobs:
- `id`: Primary key
- `job_name`: Unique job identifier
- `service_name`: Target service name
- `url`: Target service URL
- `port`: Target service port

#### JobStatus
Represents a single job execution:
- `id`: Primary key
- `uuid`: Unique execution identifier
- `status`: Execution status (STARTED, IN_PROGRESS, SUCCESS, FAILED, CANCELLED)
- `response`: Response body from remote service
- `http_status`: HTTP status code from remote service
- `start_date`: Job execution start timestamp
- `end_date`: Job execution end timestamp
- `idempotency_key`: Idempotency key for duplicate prevention

**Indexes:**
- `idx_uuid`: On `uuid` column
- `idx_idempotency_key`: On `idempotency_key` column

### MongoDB (Document Store)

#### MongoDocument
Generic document storage for JSON data:
- `id`: MongoDB document ID
- `documentKey`: Unique document key for lookup
- `data`: Map<String, Object> containing JSON data
- `createdAt`: Document creation timestamp
- `updatedAt`: Document last update timestamp

**Collection:** `documents`

The MongoDB service (`MongoDbService`) provides methods to:
- Store and retrieve JSON documents
- Query documents by key
- Convert objects to/from JSON
- Check document existence

## Configuration

### Application Properties

Key configuration in `application.properties`:

```properties
# Async Configuration
async.executor.core-pool-size=5
async.executor.max-pool-size=10
async.executor.queue-capacity=100
async.executor.thread-name-prefix=job-executor-

# Remote Client Configuration
remote.client.connect-timeout=5000
remote.client.read-timeout=30000
remote.client.retry.max-attempts=3
remote.client.retry.backoff-delay=1000

# Database (H2 for JPA entities)
spring.datasource.url=jdbc:h2:mem:jobdb
spring.jpa.hibernate.ddl-auto=update

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/job-orchestrator
spring.data.mongodb.auto-index-creation=true
```

### Environment-Specific Configuration

- **Development**: H2 in-memory database
- **Test**: H2 in-memory database with `application-test.properties`
- **Production**: Configure appropriate database (PostgreSQL, MySQL, etc.)

## Running the Application

### Prerequisites
- Java 21+
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)

### Development Mode

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on port 8080.

### Docker Deployment

#### Using Docker Compose (Recommended)

The `docker-compose.yml` file includes both the application and MongoDB services:

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

**Services:**
- **Application**: Available at `http://localhost:8080`
- **MongoDB**: Available at `mongodb://localhost:27017`
  - Username: `admin`
  - Password: `admin123`
  - Database: `job-orchestrator`

#### Development Mode with MongoDB Only

If you want to run the application locally but use MongoDB in Docker:

```bash
# Start only MongoDB
docker-compose -f docker-compose.dev.yml up -d

# Run application locally
mvn spring-boot:run
```

#### Building Docker Image Manually

```bash
# Build the Docker image
docker build -t job-orchestrator:1.0.0 .

# Run the container (requires MongoDB to be running)
docker run -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI=mongodb://admin:admin123@host.docker.internal:27017/job-orchestrator?authSource=admin \
  job-orchestrator:1.0.0
```

#### MongoDB Connection

The application connects to MongoDB using the connection string:
- **Docker**: `mongodb://admin:admin123@mongodb:27017/job-orchestrator?authSource=admin`
- **Local**: `mongodb://localhost:27017/job-orchestrator`

You can override the MongoDB URI using the `SPRING_DATA_MONGODB_URI` environment variable.

### Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Test

# Run only integration tests
mvn test -Dtest=*IntegrationTest
```

## API Documentation

### Swagger UI

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON

Access OpenAPI specification at:
```
http://localhost:8080/api-docs
```

## Testing

### Unit Tests

Comprehensive unit tests cover:
- **JobControllerTest**: All controller endpoints with mocked services
- **JobOrchestrationServiceTest**: Orchestration logic, idempotency, cancellation
- **JobMappingServiceTest**: Mapping resolution
- **HttpForwardingServiceTest**: Request forwarding logic
- **GlobalExceptionHandlerTest**: Exception handling

### Integration Tests

Integration tests (`JobOrchestrationIntegrationTest`) cover:
- Full job creation flow
- Idempotency scenarios
- Job status retrieval
- Job cancellation
- Error handling
- Validation scenarios

Run with:
```bash
mvn test -Dtest=JobOrchestrationIntegrationTest
```

## Error Handling

The service uses domain-specific exceptions with proper HTTP status codes:

- **JobMappingNotFoundException** → `400 BAD_REQUEST`
- **JobNotFoundException** → `404 NOT_FOUND`
- **ValidationException** → `400 BAD_REQUEST`
- **ConstraintViolationException** → `400 BAD_REQUEST`
- **Generic exceptions** → `500 INTERNAL_SERVER_ERROR`

All errors return structured error responses:
```json
{
  "errorCode": "JOB_NOT_FOUND",
  "message": "Job not found for UUID: abc-123",
  "timestamp": "2024-01-01T10:00:00"
}
```

## Validation

All API inputs are validated using Jakarta Bean Validation:

- **Job name**: Required, max 255 characters
- **Idempotency-Key**: Required, max 255 characters
- **UUID**: Required, must match UUID format pattern

Validation failures return `400 BAD_REQUEST` with detailed error messages.

## Logging

Structured logging is implemented throughout:
- Job creation events
- Execution lifecycle transitions
- Cancellation events
- Error conditions
- State changes

Log levels:
- `INFO`: Business events (job creation, completion, cancellation)
- `DEBUG`: Detailed execution flow
- `WARN`: Validation errors, not found scenarios
- `ERROR`: Exceptions and failures

## Thread Safety

The service ensures thread safety through:
- **ConcurrentHashMap** for execution registry
- **@Transactional** for database operations
- **Synchronized access** to shared state
- **Immutable DTOs** where possible

## Performance Considerations

- **Async execution**: Non-blocking job execution
- **Database indexes**: On UUID and idempotency key
- **Connection pooling**: Configured via Spring Boot
- **Thread pool**: Configurable executor for async operations
- **Request size limits**: 10MB max in-memory size for WebClient

## Security Considerations

- **Input validation**: All inputs validated
- **SQL injection protection**: JPA parameterized queries
- **Header filtering**: Idempotency-Key not forwarded to target services
- **Error message sanitization**: No sensitive data in error responses

## Future Enhancements

Potential improvements:
- Circuit breaker pattern for remote calls
- Retry policies with exponential backoff
- Request size limits configuration
- Rate limiting
- Metrics and monitoring integration
- Distributed tracing support

## Contributing

1. Follow the three-layer architecture
2. Maintain DTO-based API (no entity leakage)
3. Add unit tests for new features
4. Add integration tests for new flows
5. Update this README for API changes
6. Follow Java coding standards and best practices

## License

[Specify your license here]

## Support

For issues and questions, please contact [your contact information]
