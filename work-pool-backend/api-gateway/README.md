# Work Pool API Gateway

## Purpose
Single entry point for all APIs and WebSocket traffic.
JWT validation is enforced here via `JwtGlobalFilter` — every request to a protected
route must carry a valid `Authorization: Bearer <token>` header.

## Maven commands
```bash
# Run locally (HTTP)
cd work-pool-backend
# First-time setup (or after mvn clean): install parent + dependent modules to local repo
mvn -pl api-gateway -am -DskipTests install

# Start gateway
mvn -pl api-gateway spring-boot:run

# Run tests
mvn -pl api-gateway test

# Run coverage (JaCoCo report + checks)
mvn -pl api-gateway verify

# Run checkstyle
mvn -pl api-gateway checkstyle:check
```

## Local run with HTTPS (self-signed certificate)

### Step 1 — Generate a self-signed PKCS12 keystore

```bash
# Run from the api-gateway module root
mkdir -p src/main/resources/ssl

keytool -genkeypair \
  -alias gateway \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -storetype PKCS12 \
  -keystore src/main/resources/ssl/gateway-keystore.p12 \
  -storepass changeit \
  -dname "CN=localhost, OU=WorkPool, O=WorkPool, L=City, S=State, C=IN"
```

> **Never commit the `.p12` file.** Add `src/main/resources/ssl/` to `.gitignore`.

### Step 2 — Start the gateway with the `ssl` Spring profile

```bash
mvn -pl api-gateway spring-boot:run \
  -Dspring-boot.run.profiles=ssl
```

The gateway now listens on **https://localhost:8443**.

### Step 3 — Trust the self-signed cert (optional, for curl / browsers)

```bash
# Export the cert from the keystore
keytool -exportcert \
  -alias gateway \
  -keystore src/main/resources/ssl/gateway-keystore.p12 \
  -storepass changeit \
  -rfc \
  -file /tmp/gateway.crt

# curl with explicit trust
curl --cacert /tmp/gateway.crt https://localhost:8443/actuator/health
```

### Docker / docker-compose with HTTPS

Pass the keystore via a bind-mount and set environment variables:

```yaml
api-gateway:
  build: ./work-pool-backend/api-gateway
  ports:
    - "8443:8443"
  environment:
    SPRING_PROFILES_ACTIVE: ssl
    SERVER_SSL_KEY_STORE: file:/certs/gateway-keystore.p12
    SERVER_SSL_KEY_STORE_PASSWORD: changeit
  volumes:
    - ./certs/gateway-keystore.p12:/certs/gateway-keystore.p12:ro
```

### Production / Kubernetes

For production or Kubernetes deployments use a proper certificate (Let's Encrypt,
cert-manager, or your organization's CA) instead of a self-signed cert.
An Istio / Ingress-based TLS termination approach is recommended for Kubernetes —
keep the inter-service communication inside the mesh.

## Default port
- HTTP : `8080`
- HTTPS: `8443` (with `ssl` profile)

## Required env
- `JWT_SECRET` — must be ≥ 32 bytes
- `USER_SERVICE_URL`
- `TASK_SERVICE_URL`
- `NOTIFICATION_SERVICE_URL`
- `PAYMENT_SERVICE_URL`
- `RATING_SERVICE_URL`

## Key routes
- `/api/v1/auth/**`, `/api/v1/users/**` → user service (8081)
- `/api/v1/tasks/**` → task service (8082)
- `/api/v1/notifications/**`, `/ws/**` → notification service (8083)
- `/api/v1/payments/**` → payment service (8084)
- `/api/v1/ratings/**` → rating service (8085)
