# Shipping Service — Spring MVC Mock

Java 17 / Spring Boot 4.1.1 / PostgreSQL. Independent module.

## Run

`docker compose up --build -d`
or `docker compose up -d db` then `mvn spring-boot:run`.

## API

POST `http://localhost:8086/api/shipments`

```json
{"orderId":"a01cdef0-1234-4567-890a-123456789abc","shippingAddress":"Ha Noi"}
```

GET `/api/shipments/{trackingCode}` or `/api/shipments?orderId={uuid}`.

PENDING at T=0, SHIPPED at about T=3 seconds, DELIVERED about T=6 seconds.
Scheduler checks every 500ms. Shipment creation is idempotent by orderId.
DB persists status across restarts. No authentication (demo only).
For Order Service: POST only after InventoryReserved/CONFIRMED; poll GET to sync status.
Order must transition CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED, without skipping.
Use outbox and retries for reliable cross-service integration.
Production: add service auth, Flyway, distributed scheduler locking, error handling and observability.
