# Work Pool Task Service

## Purpose
Task lifecycle, bidding, assignment, completion flow, and publisher-finisher message triggers.

## Maven commands
```bash
# Run locally
cd /home/runner/work/work-pool/work-pool/work-pool-backend
mvn -pl work-pool-task-service spring-boot:run

# Run tests
mvn -pl work-pool-task-service test

# Run coverage (JaCoCo report + checks)
mvn -pl work-pool-task-service verify

# Run checkstyle
mvn -pl work-pool-task-service checkstyle:check
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
