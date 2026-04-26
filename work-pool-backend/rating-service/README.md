# Work Pool Rating Service

## Purpose
Handles post-completion ratings/reviews and user trust score updates.

## Maven commands
```bash
# Run locally
cd work-pool-backend
mvn -pl rating-service spring-boot:run

# Run tests
mvn -pl rating-service test

# Run coverage (JaCoCo report + checks)
mvn -pl rating-service verify

# Run checkstyle
mvn -pl rating-service checkstyle:check
```

## Default port
- `8085`

## Required env
- `SPRING_DATA_MONGODB_URI`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`

## Key flow
- Accepts rating submissions and publishes `workpool.rating.submitted` events.
