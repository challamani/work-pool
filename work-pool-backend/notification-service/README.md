# Work Pool Notification Service

## Purpose
Persists and delivers real-time notifications using Kafka + WebSocket (STOMP/SockJS).

## Maven commands
```bash
# Run locally
cd work-pool-backend
mvn -pl notification-service spring-boot:run

# Run tests
mvn -pl notification-service test

# Run coverage (JaCoCo report + checks)
mvn -pl notification-service verify

# Run checkstyle
mvn -pl notification-service checkstyle:check
```

## Default port
- `8083`

## Required env
- `SPRING_DATA_MONGODB_URI`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`

## Key flows
- Consumes `workpool.notification.send` events.
- Pushes user notifications to `/user/{userId}/queue/notifications`.
