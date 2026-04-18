# Work Pool Rating Service

## Purpose
Handles post-completion ratings/reviews and user trust score updates.

## Local run
```bash
cd /home/runner/work/work-pool/work-pool/work-pool-backend
mvn -pl work-pool-rating-service spring-boot:run
```

## Default port
- `8085`

## Required env
- `SPRING_DATA_MONGODB_URI`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`

## Key flow
- Accepts rating submissions and publishes `workpool.rating.submitted` events.
