# Work Pool User Service

## Purpose
Authentication, user registration/login, profile management, and login audit tracking.

## Maven commands
```bash
# Run locally
cd work-pool-backend
# Use Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

# First-time setup (or after mvn clean): install parent + dependent modules
mvn -pl work-pool-user-service -am -DskipTests install

# Start service
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
- `SPRING_DATA_MONGODB_URI` (if using docker-compose Mongo, use `mongodb://workpool:placeholder@localhost:27017/workpool_users?authSource=admin`)
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `FACEBOOK_CLIENT_ID`, `FACEBOOK_CLIENT_SECRET`
- `HAZELCAST_CLUSTER_MEMBERS` (optional; leave empty for local startup)

## Security notes
- Password logins are rate-limited with temporary account lockout after repeated failures.
- Each login attempt is audited with client IP and request headers for fraud analysis.
- Public profile endpoint masks sensitive contact/location details.
