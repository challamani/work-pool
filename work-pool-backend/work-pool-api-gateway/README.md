# Work Pool API Gateway

## Purpose
Single entry point for all APIs and WebSocket traffic.

## Local run
```bash
cd /home/runner/work/work-pool/work-pool/work-pool-backend
mvn -pl work-pool-api-gateway spring-boot:run
```

## Default port
- `8080`

## Required env
- `JWT_SECRET`
- `USER_SERVICE_URL`
- `TASK_SERVICE_URL`
- `NOTIFICATION_SERVICE_URL`
- `PAYMENT_SERVICE_URL`
- `RATING_SERVICE_URL`

## Key routes
- `/api/v1/auth/**`, `/api/v1/users/**` -> user service
- `/api/v1/tasks/**` -> task service
- `/api/v1/notifications/**`, `/ws/**` -> notification service
- `/api/v1/payments/**` -> payment service
- `/api/v1/ratings/**` -> rating service
