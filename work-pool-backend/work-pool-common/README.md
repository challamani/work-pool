# Work Pool Common Module

## Purpose
Shared enums, events, DTOs, exceptions, and constants used by all backend services.

## Validate locally
```bash
cd /home/runner/work/work-pool/work-pool/work-pool-backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn -pl work-pool-common clean verify
```

## Coverage
- This module enforces JaCoCo quality gates (line + branch coverage).
