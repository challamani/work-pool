# Work Pool Notification Service

## Purpose
Persists and delivers real-time notifications using Kafka + WebSocket (STOMP/SockJS).

## Local run
```bash
cd /home/runner/work/work-pool/work-pool/work-pool-backend
mvn -pl work-pool-notification-service spring-boot:run
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
