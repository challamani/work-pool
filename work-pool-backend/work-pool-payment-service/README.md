# Work Pool Payment Service

## Purpose
Escrow and payout orchestration with commission handling and Razorpay integration.

## Local run
```bash
cd /home/runner/work/work-pool/work-pool/work-pool-backend
mvn -pl work-pool-payment-service spring-boot:run
```

## Default port
- `8084`

## Required env
- `SPRING_DATA_MONGODB_URI`
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`
- `RAZORPAY_KEY_ID`
- `RAZORPAY_KEY_SECRET`
- `RAZORPAY_WEBHOOK_SECRET`

## Local test payments
- Use Razorpay **test** keys (`rzp_test_*`).
- Create order via `POST /api/v1/payments/orders`.
- Complete payment using Razorpay test mode cards/UPI.
- Simulate webhook to local endpoint `POST /api/v1/payments/webhook`.
