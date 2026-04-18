# Work Pool

> **Connecting task publishers with skilled workers across India.**  
> Work Pool is a task marketplace where anyone can post a task publicly and skilled workers in the region can bid, get hired, and earn money — with full escrow protection and 1% platform commission from both sides.

---

## 🗂️ Monorepo Structure

```
work-pool/
├── work-pool-backend/            # Spring Boot multi-module backend (Maven)
│   ├── pom.xml                   # Parent POM
│   ├── work-pool-common/         # Shared models, DTOs, events, exceptions
│   ├── work-pool-api-gateway/    # Spring Cloud Gateway (port 8080)
│   ├── work-pool-user-service/   # Auth, profiles, skills (port 8081)
│   ├── work-pool-task-service/   # Task marketplace, bids (port 8082)
│   ├── work-pool-notification-service/  # WebSocket + Kafka (port 8083)
│   ├── work-pool-payment-service/       # Razorpay escrow, wallet (port 8084)
│   └── work-pool-rating-service/        # Ratings, trust profile (port 8085)
└── work-pool-ui/                 # React + TypeScript + Vite + Tailwind CSS
```

---

## 🚀 Quick Start (Docker Compose)

```bash
# 1. Copy and configure environment
cp .env.example .env          # Set RAZORPAY_KEY_ID, JWT_SECRET, OAuth2 keys

# 2. Start all infrastructure + services
docker-compose up -d

# 3. Access
#   Frontend:  http://localhost:3000
#   API:       http://localhost:8080
#   Mailhog:   http://localhost:8025
#   Hazelcast: http://localhost:8090
```

### Or run services individually:
```bash
# Backend (from work-pool-backend/)
mvn clean package -DskipTests
cd work-pool-user-service && java -jar target/work-pool-user-service-*.jar

# Frontend (from work-pool-ui/)
npm install && npm run dev
```

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS, React Query, Zustand |
| **Backend** | Spring Boot 3.2, Spring Cloud Gateway, Spring Security |
| **Auth** | JWT + OAuth2 (Google, Facebook) |
| **Messaging** | Apache Kafka |
| **Database** | MongoDB (per service) |
| **Cache/Distributed** | Hazelcast 5 |
| **Payments** | Razorpay (escrow + UPI/cards) |
| **Notifications** | WebSocket (STOMP) + Kafka |
| **API Docs** | SpringDoc OpenAPI / Swagger UI |

---

## 📋 Core Features (MVP)

### Task Marketplace
- Post tasks with category, location (city/district/state), budget range, and schedule
- 20+ task categories: Home Repair, Cleaning, Teaching, Cooking, Moving, IT Support...
- Skill + location matching → notify nearby qualified workers
- Full bid lifecycle: place → accept → reject → withdraw

### Task Lifecycle
```
OPEN → BIDDING → ASSIGNED → IN_PROGRESS → PENDING_REVIEW → COMPLETED
                                                          ↘ DISPUTED
```

### Payments (Escrow)
- Publisher creates a Razorpay order; funds held in escrow
- Platform takes **1% from publisher** + **1% from finisher** (total 2% of agreed amount)
- Escrow releases to finisher only after publisher confirms completion

### Identity & Trust
- Google / Facebook OAuth2 login + email/password
- Profile: name, mobile, skills, city/district/state, service radius
- Aadhaar verification workflow (UNVERIFIED → PENDING → VERIFIED)
- Post-completion star ratings (1–5) and written reviews
- Aggregate trust score displayed on public profile

### Notifications
- Real-time WebSocket (STOMP) notifications for matched tasks, bid updates, payment events
- Persistent notification log with unread count badge

---

## 🔑 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | Shared JWT signing secret (≥32 chars) | `changeme_...` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | placeholder |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | placeholder |
| `FACEBOOK_CLIENT_ID` | Facebook OAuth2 app ID | placeholder |
| `FACEBOOK_CLIENT_SECRET` | Facebook OAuth2 secret | placeholder |
| `RAZORPAY_KEY_ID` | Razorpay test/live key | `rzp_test_...` |
| `RAZORPAY_KEY_SECRET` | Razorpay secret | placeholder |

---

## 📝 API Overview

All requests go through the API Gateway at `http://localhost:8080`.

| Service | Base Path | Key Endpoints |
|---------|-----------|---------------|
| Auth | `/api/v1/auth` | `POST /register`, `POST /login`, OAuth2 flows |
| Users | `/api/v1/users` | `GET /me`, `PUT /me`, `GET /{id}` |
| Tasks | `/api/v1/tasks` | `GET /`, `POST /`, `GET /{id}`, bid APIs |
| Notifications | `/api/v1/notifications` | `GET /`, `PATCH /{id}/read`, WebSocket `/ws` |
| Payments | `/api/v1/payments` | `POST /orders`, `POST /webhook`, `GET /wallet` |
| Ratings | `/api/v1/ratings` | `POST /`, `GET /users/{id}`, `GET /users/{id}/summary` |

Swagger UI for each service: `http://localhost:{port}/swagger-ui.html`

---

## 🗓️ Phased Roadmap

- [x] Phase 1 – Monorepo baseline (Spring Boot multi-module + React)
- [x] Phase 2 – Core identity and onboarding (JWT + OAuth2 + profile)
- [x] Phase 3 – Task marketplace core (CRUD + bid/accept lifecycle)
- [x] Phase 4 – Wallet/escrow and 1% commission (Razorpay)
- [x] Phase 5 – Notifications (WebSocket + Kafka)
- [x] Phase 6 – Ratings and trust profile
- [ ] Phase 7 – Admin panel + fraud controls
- [ ] Phase 8 – AI features: voice interaction, auto-categorization

