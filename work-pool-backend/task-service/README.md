# Work Pool Task Service

## Purpose
Task lifecycle, bidding, assignment, completion flow, and publisher-finisher message triggers.

## Maven commands
```bash
# Run locally
cd work-pool-backend
mvn -pl task-service spring-boot:run

# Run tests
mvn -pl task-service test

# Run coverage (JaCoCo report + checks)
mvn -pl task-service verify

# Run checkstyle
mvn -pl task-service checkstyle:check
```

## Default port
- `8082`

## Required env
- `SPRING_DATA_MONGODB_URI`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`
- `USER_SERVICE_URL`

## Messaging integration
- After assignment, publisher and assigned finisher can exchange direct messages via:
  - `POST /api/v1/tasks/{taskId}/messages`
- Messages are published to `workpool.notification.send` and delivered by notification service.
