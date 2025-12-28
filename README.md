# Job Orchestrator Service

A production-grade Java 21 Spring Boot microservice that orchestrates job execution across downstream microservices.

## Architecture

The service follows a clean three-layer architecture:

- **API Layer**: REST controllers with DTOs
- **Application/Domain Layer**: Business logic and orchestration services
- **Persistence Layer**: JPA repositories

## Features

- Asynchronous job execution
- Job-to-service mapping resolution
- Execution lifecycle tracking
- RESTful API with polling support
- Comprehensive error handling
- Structured logging
- Transaction management
- Retry mechanism for remote calls
- Timeout configuration
- Swagger/OpenAPI documentation

## API Endpoints

### POST /jobs
Creates and triggers a job execution.

**Request:**
```json
{
  "jobName": "data-processing"
}
```

**Response:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000"
}
```

### GET /jobs/{uuid}
Retrieves the status of a job execution.

**Response:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "response": "...",
  "httpStatus": 200,
  "startDate": "2024-01-01T10:00:00",
  "endDate": "2024-01-01T10:00:15"
}
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8080.

## Swagger Documentation

Once running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Configuration

Key configuration properties in `application.yml`:

- `async.executor.*`: Thread pool configuration for async execution
- `remote.client.*`: Remote client timeout and retry settings

## Database

Uses H2 in-memory database for development. Sample job mappings are loaded via `data.sql`.

## Job Execution Flow

1. Client sends POST /jobs with job name
2. Service resolves job mapping
3. JobStatus entity created with STARTED status
4. UUID returned immediately to client
5. Job executes asynchronously:
   - Status updated to IN_PROGRESS
   - Remote REST call executed
   - Status updated to SUCCESS or FAILED
   - Response and HTTP status persisted
6. Client polls GET /jobs/{uuid} for status

