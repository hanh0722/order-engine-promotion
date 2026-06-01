# Order Engine — Order Pricing & Promotion Engine

Senior Software Engineer take-home assignment implementation for **Challenge 2: Order Pricing & Promotion Engine**.

A Spring Boot service that calculates order totals by applying multiple promotion rules in sequence. Rules are pluggable (Strategy + Chain of Responsibility), promotions and coupons are stored in PostgreSQL, and schema changes are managed with Liquibase SQL changesets.

---

## 1. Challenge choice

**Challenge 2 — Order Pricing & Promotion Engine**

This challenge maps well to composable business rules: each promotion is an independent policy, new rules can be added without editing existing ones, and the problem naturally fits **Strategy** (one algorithm per rule) and **Chain of Responsibility** (rules applied in order). It also exercises API design, persistence, and clear pricing semantics without distributed locking.

---

## 2. Architecture overview

The project uses a **layered / hexagonal-style** layout: HTTP adapters at the edge, application services orchestrating use cases, and domain logic that does not depend on Spring or JPA.

```
┌─────────────────────────────────────────────────────────────┐
│  api/          Controllers, DTOs, application services      │
├─────────────────────────────────────────────────────────────┤
│  domain/       Promotion rules, pipeline, domain models     │
├─────────────────────────────────────────────────────────────┤
│  entity/       JPA entities                                 │
│  repository/   Spring Data JPA                              │
├─────────────────────────────────────────────────────────────┤
│  infrastructure/   Spring configuration (pipeline wiring)   │
└─────────────────────────────────────────────────────────────┘
         │                              │
         ▼                              ▼
   PostgreSQL                    Liquibase (SQL)
```

| Package | Responsibility |
|---------|----------------|
| `api.controller` | REST endpoints (`/api/v1/orders`, `/api/v1/promotions`) |
| `api.service` | `OrderService`, `PromotionService` — load data, run pipeline, persist orders |
| `api.dto` | Request/response models and `BaseResponse` envelope |
| `domain.promotion.strategy` | One class per promotion rule |
| `domain.promotion.chain` | Chain handlers delegating to strategies |
| `domain.promotion` | `PromotionPipeline`, `PromotionType` |
| `domain.model` | `PromotionContext`, `PromotionDetail` — calculation state |
| `entity` / `repository` | Persistence |
| `infrastructure.configuration` | Wires the promotion chain at startup |

**Request flow (calculate price)**

1. `OrderController` receives `POST /api/v1/orders/calculate`.
2. `OrderService` validates coupon (if present), loads active promotions from DB, builds `PromotionContext`.
3. `PromotionPipeline` walks the handler chain; each handler invokes its `PromotionStrategy`.
4. Discount line items are aggregated; order + line items are persisted.
5. `CreateOrderResponse` is returned with subtotal, discounts, total discount, and final price.

---

## 3. Design patterns

### Strategy pattern

Each promotion rule implements `PromotionStrategy` with a single `apply(PromotionContext)` method.

| Rule | Class |
|------|--------|
| 10% order discount | `domain/promotion/strategy/PercentageDiscountStrategy.java` |
| Buy 2 get 1 free (per SKU) | `domain/promotion/strategy/Buy2Get1Strategy.java` |
| VIP extra 5% | `domain/promotion/strategy/VipCustomerStrategy.java` |
| Fixed coupon | `domain/promotion/strategy/CouponStrategy.java` |

Interface: `domain/promotion/strategy/PromotionStrategy.java`

Adding a new rule: implement `PromotionStrategy`, register a `PromotionStrategyHandler` in `infrastructure/configuration/PromotionPipelineConfiguration.java` — no changes to existing strategy classes.

### Chain of Responsibility

Handlers are linked in order; each passes control to the next after applying its rule.

| Piece | Location |
|-------|----------|
| Abstract handler | `domain/promotion/chain/PromotionHandler.java` |
| Strategy adapter | `domain/promotion/chain/PromotionStrategyHandler.java` |
| Pipeline entry | `domain/promotion/PromotionPipeline.java` |
| Chain wiring (order) | `infrastructure/configuration/PromotionPipelineConfiguration.java` |

**Pipeline order:** Percentage → Buy 2 Get 1 → Coupon → VIP

---

## 4. SOLID principles (where they show up)

| Principle | Example in this codebase |
|-----------|---------------------------|
| **S** — Single responsibility | Each `*Strategy` class owns one discount rule only; `OrderService` orchestrates, it does not encode rule math. |
| **O** — Open/closed | New promotions via new `PromotionStrategy` + handler link; existing strategies stay unchanged. |
| **L** — Liskov substitution | Any `PromotionStrategy` can be wrapped by `PromotionStrategyHandler` without breaking the chain. |
| **I** — Interface segregation | `PromotionStrategy` exposes only `getPromotionType()` and `apply()` — no persistence or HTTP concerns. |
| **D** — Dependency inversion | `OrderService` depends on `PromotionPipeline` and repositories (abstractions via Spring injection), not concrete strategy classes. |

---

## 5. Database design

Schema is defined in Liquibase **SQL-only** changesets:

- `src/main/resources/db/changelog/changes/001-create-schema.sql`
- `src/main/resources/db/changelog/changes/002-seed-data.sql`

| Table | Purpose |
|-------|---------|
| `products` | Catalog SKU, name, unit price (seeded for reference; calculate API accepts prices in the request body). |
| `promotions` | Configurable rules: `type`, `value`, `active`, timestamps. Types align with `PromotionType` enum. |
| `coupons` | `code`, fixed `discount_amount`, `active`, `expiry_date`. Unique index on `code`. |
| `orders` | Persisted calculation: `customer_type`, `sub_total`, `total_discount`, `final_price`. |
| `order_items` | Line items per order (`sku`, `price`, `quantity`), FK to `orders` with cascade delete. |

**Decisions**

- **Numeric(19,2)** for money columns to avoid floating-point errors.
- **Indexes** on `promotions(active)`, `coupons(code)`, `order_items(order_id)` for common lookups.
- **Seed data** loads default promotions (`PERCENTAGE_DISCOUNT` 10%, `VIP_DISCOUNT` 5%, `BUY2_GET1_FREE`) and coupons `SUMMER10` / `SAVE20` so the service works out of the box after migrations.
- **JPA `ddl-auto: validate`** — Hibernate never mutates schema; Liquibase is the single source of truth.

---

## 6. How to run the system

### Option A — Docker Compose (recommended for reviewers)

From the repository root:

```bash
docker compose up --build
```

This starts PostgreSQL (with healthcheck) and the Spring Boot app. Liquibase migrations run on first startup. The API is available at **http://localhost:8080**.

### Option B — Local development

**Prerequisites:** Java 17, Maven 3.9+, PostgreSQL 16+

1. **Start PostgreSQL** (example with Docker):

```bash
docker run -d --name order-engine-db \
  -e POSTGRES_USER=root \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=order_engine_db \
  -p 5432:5432 \
  postgres:16
```

2. **Run the application:**

```bash
./mvnw spring-boot:run
```

Default datasource settings are in `src/main/resources/application.yml` (`localhost:5432`, database `order_engine_db`, user `root` / `password`).

Liquibase runs migrations on startup.

### API examples

**Calculate order price** (assignment example)

```bash
curl -s -X POST http://localhost:8080/api/v1/orders/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "customerType": "VIP",
    "items": [
      { "sku": "A100", "price": 100, "quantity": 2 },
      { "sku": "B200", "price": 50,  "quantity": 1 }
    ],
    "couponCode": "SUMMER10"
  }'
```

Expected discount breakdown (subtotal **250**):

| Type | Amount |
|------|--------|
| `PERCENTAGE_DISCOUNT` | 25.00 |
| `BUY2_GET1_FREE` | 100.00 |
| `COUPON_SUMMER10` | 10.00 |
| `VIP_DISCOUNT` | 12.50 |
| **Total discount** | **147.50** |
| **Final price** | **102.50** |

**List active promotions**

```bash
curl -s http://localhost:8080/api/v1/promotions
```

**Create a promotion**

```bash
curl -s -X POST http://localhost:8080/api/v1/promotions \
  -H "Content-Type: application/json" \
  -d '{ "type": "PERCENTAGE_DISCOUNT", "value": 10, "active": true }'
```

### Response envelope

Promotion endpoints use the required envelope via `BaseResponse`:

```json
{ "data": { ... }, "error": null }
```

The calculate endpoint currently returns `CreateOrderResponse` directly; wrapping it in `BaseResponse` for full consistency across all routes is listed under improvements below.

---

## 7. How to run tests

```bash
./mvnw test
```

| Test type | Status / intent |
|-----------|------------------|
| **Unit tests (service + strategies)** | Assignment expects Mockito-based tests per rule and invalid paths — extend under `src/test/java/.../domain/promotion/strategy/`. |
| **Integration test (Testcontainers)** | Assignment expects full calculate flow against real PostgreSQL — add under `src/test/java/...` with `@Testcontainers`. |
| **Smoke** | `OrderEngineApplicationTests` — Spring context load. |

Example (once added):

```bash
./mvnw test -Dtest=PercentageDiscountStrategyTest
./mvnw test -Dtest=OrderCalculateIntegrationTest
```

---

## 8. Trade-offs and future improvements

| Area | Current choice | With more time |
|------|----------------|----------------|
| **Discount base** | VIP and percentage use **order subtotal**; rules do not stack on a running “price after discounts” total. | Configurable stacking (pre-tax subtotal vs. net-after-each-rule). |
| **API envelope** | Promotions use `BaseResponse`; calculate does not yet. | Global `@ControllerAdvice` + wrap all endpoints. |
| **Error codes** | `GeneralException` for invalid coupons; no structured `code` field everywhere. | Map exceptions to `BaseError` with stable codes (`INVALID_COUPON`, etc.). |
| **Buy 2 Get 1** | `quantity / 2` free units per SKU (integer division). | Align with product catalog prices from DB instead of request-only prices. |
| **Pipeline `supports()`** | Defined on `PromotionHandler` but chain always invokes `doHandle`. | Gate inactive promotion types explicitly in the chain. |
| **Tests** | Minimal context smoke test. | Full strategy unit suite + Testcontainers integration per assignment spec. |
| **Docker** | `docker compose up` runs app + Postgres with healthchecks. | Multi-stage build cache tuning; non-root user already used in image. |
| **Caching** | Caffeine configured; promotions loaded from DB each request. | Cache active promotions/coupons with TTL. |

---

## 9. What breaks at scale and mitigations

| Risk | Why | Mitigation |
|------|-----|------------|
| **Synchronous calculate** | Every request loads all active promotions and writes an order row. | Cache promotion catalog; async order persistence or event outbox. |
| **Rule chain in memory** | Pipeline rebuilt per JVM; order fixed in config. | Externalize rule order; feature flags per tenant. |
| **DB hot rows** | High write rate on `orders` / `order_items`. | Partition by date; archive; read replicas for reporting. |
| **Coupon abuse** | No rate limiting or single-use enforcement. | Redemption table with unique `(coupon_id, customer_id)`; idempotency keys on calculate. |
| **Numeric consistency** | Multiple discounts summed in Java. | Central money service; round once at presentation boundary. |
| **Single instance** | No distributed concerns today. | Stateless pods behind load balancer; shared Postgres; in-memory or distributed cache for promotions. |

---

## Tech stack

- Java 17
- Spring Boot 3.5.x
- Spring Data JPA
- PostgreSQL
- Liquibase (SQL changesets)
- Maven
- Lombok
- JUnit 5 (tests)

---

## Project layout (quick reference)

```
src/main/java/com/engine/order_engine/
├── api/                    # REST + services + DTOs
├── domain/                 # Promotion rules & pipeline
├── entity/                 # JPA entities
├── repository/             # Spring Data
├── infrastructure/         # Bean configuration
└── exception/
src/main/resources/
├── application.yml
└── db/changelog/           # Liquibase SQL
src/test/java/              # Tests
```

---

## License / submission

Push this repository to a **public GitHub** repo and share the link as required by the assignment brief.
