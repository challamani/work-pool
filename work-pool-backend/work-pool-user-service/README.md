# Work Pool User Service

## Purpose
Authentication, user registration/login, profile management, and login audit tracking.

## Maven commands
```bash
# Run locally
cd work-pool-backend
mvn -pl work-pool-user-service spring-boot:run

# Run tests
mvn -pl work-pool-user-service test

# Run coverage (JaCoCo report + checks)
mvn -pl work-pool-user-service verify

# Run checkstyle
mvn -pl work-pool-user-service checkstyle:check
```

## Default port
- `8081`

## Required env
- `SPRING_DATA_MONGODB_URI`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `FACEBOOK_CLIENT_ID`, `FACEBOOK_CLIENT_SECRET`

## Security notes
- Password logins are rate-limited with temporary account lockout after repeated failures.
- Each login attempt is audited with client IP and request headers for fraud analysis.
- Public profile endpoint masks sensitive contact/location details.
