# Order Engine — Order Pricing & Promotion Engine

Spring Boot service for **Challenge 2** of the Senior Software Engineer take-home: calculate order totals by applying multiple promotion rules, persist orders, and manage promotions/coupons in PostgreSQL.

**Tech stack:** Java 17 · Spring Boot 3.5 · Spring Data JPA · PostgreSQL · Liquibase (SQL) · Maven · JUnit 5 · Mockito · Docker Compose

---

## 1. Which challenge and why

**Challenge 2 — Order Pricing & Promotion Engine**

This option fits a rule-composition problem: each discount is independent, new rules should plug in without changing existing code, and the assignment maps cleanly to **Strategy** (one algorithm per rule) and **Chain of Responsibility** (rules applied in sequence). It still covers REST APIs, persistence, migrations, and tests—without inventory locking, which is the focus of Challenge 1.

---

## 2. Architecture overview

This is a **Spring Boot layered application** (controller → service → repository → database), not strict hexagonal/clean architecture. The interesting part is a **promotion pricing engine** living under `domain.promotion`, which `OrderService` calls after loading data from JPA.

### Layers and dependency direction

```
  ┌─────────────────────────────────────────────────────────────┐
  │  Presentation                                                │
  │  api.controller          api.dto (request/response envelopes)  │
  └──────────────────────────────┬──────────────────────────────┘
                                 │ calls
  ┌──────────────────────────────▼──────────────────────────────┐
  │  Application (Spring @Service)                               │
  │  api.service — OrderService, PromotionService                │
  │  • @Transactional boundaries                                 │
  │  • loads/saves via repositories                              │
  │  • builds PromotionContext, invokes PromotionPipeline        │
  └───────┬──────────────────────────────┬──────────────────────┘
          │                              │
          │ uses                         │ uses
          ▼                              ▼
  ┌───────────────────┐      ┌──────────────────────────────────┐
  │  Persistence      │      │  Promotion engine (domain.*)      │
  │  repository       │      │  PromotionPipeline                │
  │  entity (JPA)     │◄─────│  chain → PromotionStrategy       │
  └─────────┬─────────┘      │  PromotionContext / Detail       │
            │                └──────────────────────────────────┘
            │                          ▲
            │                          │ reads Coupon, Promotion
            ▼                          │ (JPA entities passed in)
  ┌───────────────────┐      ┌─────────┴────────────────────────┐
  │  PostgreSQL       │      │  Also uses: domain.customer,     │
  │  (Liquibase)      │      │  domain.dto.OrderItemRequest     │
  └───────────────────┘      └──────────────────────────────────┘

  Cross-cutting: exception (BusinessException, GlobalException)
  Bootstrap:     infrastructure.configuration — registers PromotionPipeline @Bean
```

**Important:** `domain` is **not** isolated from persistence. `PromotionContext` and several strategies take JPA types (`entity.Coupon`, `entity.Promotion`) directly. Repositories and entities sit beside the promotion engine, not behind ports/adapters.

### Two entry points

| Path | Flow |
|------|------|
| **Calculate order** | `OrderController` → `OrderService` → repositories (read coupon/promotions) → `PromotionPipeline` → `PromotionService.getTotalDiscount()` → repository (save order) → `BaseResponse<CreateOrderResponse>` |
| **Manage promotions** | `PromotionController` → `PromotionService` → `PromotionRepository` (list active / create row). Does not run the pricing pipeline. |

### Package map (what each folder actually does)

| Package | Role |
|---------|------|
| `api.controller` | REST mapping, wraps results in `BaseResponse` |
| `api.service` | Application logic and transactions; **only layer that coordinates DB + pipeline** |
| `api.dto` | HTTP contracts: `CalculateOrderRequest`, `BaseResponse`, `CreateOrderResponse` |
| `domain.promotion` | Pricing pipeline: `PromotionPipeline`, `PromotionType`, handler chain, strategies |
| `domain.model` | In-memory calculation state: `PromotionContext`, `PromotionDetail` |
| `domain.dto` / `domain.customer` | Shared input types (`OrderItemRequest`, `CustomerType`) used by API requests and `PromotionContext` |
| `entity` | JPA mappings for `orders`, `promotions`, `coupons`, etc. |
| `repository` | Spring Data JPA queries |
| `infrastructure.configuration` | Builds the `PromotionPipeline` bean (plain `new` on strategies, linked handlers) |
| `exception` | `BusinessException` + `@RestControllerAdvice` for error envelope |

### How the promotion engine is wired

Strategies are **not** Spring beans individually. `PromotionPipelineConfiguration` instantiates them, wraps each in a `PromotionStrategyHandler`, links the chain, and exposes a single `PromotionPipeline` bean. `OrderService` receives that bean via constructor injection.

### Calculate flow (step by step)

1. `OrderController` validates `CalculateOrderRequest` and delegates to `OrderService.calculate()`.
2. `OrderService` loads coupon (if `couponCode` present) and active `Promotion` rows from the database.
3. It constructs `PromotionContext` (computes subtotal from line items).
4. `PromotionPipeline.process(context)` walks the chain: Percentage → Buy2Get1 → Coupon → VIP.
5. Each strategy returns an optional `PromotionDetail`; `PromotionService.getTotalDiscount()` sums amounts.
6. `OrderService` maps request items to `Order` / `OrderItem` entities and saves via `OrderRepository`.
7. Response is built as `CreateOrderResponse` inside `BaseResponse`.

### Design intent

- **Separation for pricing rules:** new discounts = new `PromotionStrategy` + one line in pipeline config; `OrderService` stays thin.
- **Pragmatic Spring style:** one service class owns the use case; repositories and entities are used directly without a separate domain repository interface layer.

---

## 3. Design patterns (where in code)

### Strategy

Each promotion rule implements `PromotionStrategy`:

| Rule | Implementation |
|------|----------------|
| Percentage off order | `domain/promotion/strategy/PercentageDiscountStrategy.java` |
| Buy 2 get 1 free (per SKU) | `domain/promotion/strategy/Buy2Get1Strategy.java` |
| VIP +5% | `domain/promotion/strategy/VipCustomerStrategy.java` |
| Fixed coupon | `domain/promotion/strategy/CouponStrategy.java` |

Contract: `domain/promotion/strategy/PromotionStrategy.java`

**Add a new rule:** create a strategy class + link a `PromotionStrategyHandler` in `infrastructure/configuration/PromotionPipelineConfiguration.java` without modifying existing strategies.

### Chain of Responsibility

| Component | File |
|-----------|------|
| Abstract handler (link + `handle`) | `domain/promotion/chain/PromotionHandler.java` |
| Adapter (strategy → handler) | `domain/promotion/chain/PromotionStrategyHandler.java` |
| Pipeline facade | `domain/promotion/PromotionPipeline.java` |
| Chain order (wired at startup) | `infrastructure/configuration/PromotionPipelineConfiguration.java` |

**Execution order:** `PERCENTAGE_DISCOUNT` → `BUY2_GET1_FREE` → coupon → `VIP_DISCOUNT`

---

## 4. SOLID principles (where they show up)

| Principle | Where |
|-----------|--------|
| **Single responsibility** | Each `*Strategy` implements one discount; `OrderService` orchestrates only; `PromotionService` handles promotion CRUD/summing. |
| **Open/closed** | New discounts = new strategy + handler link; existing strategy classes unchanged. |
| **Liskov substitution** | Any `PromotionStrategy` works inside `PromotionStrategyHandler` without special cases. |
| **Interface segregation** | `PromotionStrategy` only exposes `getPromotionType()` and `apply(PromotionContext)`. |
| **Dependency inversion** | `OrderService` depends on `PromotionPipeline` and repository interfaces injected by Spring, not on concrete strategy classes. |

---

## 5. Database design

Liquibase **SQL-only** changesets (no XML):

| File | Purpose |
|------|---------|
| `src/main/resources/db/changelog/db.changelog-master.yaml` | Master changelog |
| `src/main/resources/db/changelog/changes/001-create-schema.sql` | Tables + indexes |
| `src/main/resources/db/changelog/changes/002-seed-data.sql` | Sample products, promotions, coupons |

### Tables

| Table | Purpose |
|-------|---------|
| `products` | SKU, name, price (seeded; calculate API still accepts price in the request body) |
| `promotions` | `type`, `value`, `active`, timestamps — types match `PromotionType` enum |
| `coupons` | `code`, `discount_amount`, `active`, `expiry_date` — unique index on `code` |
| `orders` | `customer_type`, `sub_total`, `total_discount`, `final_price`, timestamps |
| `order_items` | `order_id`, `sku`, `price`, `quantity` — FK to `orders` with `ON DELETE CASCADE` |

### Decisions

- **`NUMERIC(19,2)`** for money — avoids floating-point drift.
- **Indexes** on `promotions(active)`, `coupons(code)`, `order_items(order_id)` / `sku`.
- **Seed data** — 10% percentage, VIP 5%, Buy2Get1 flag, coupons `SUMMER10` ($10) and `SAVE20` ($20).
- **`spring.jpa.hibernate.ddl-auto: validate`** — schema owned by Liquibase only.

---

## 6. How to run the system

### Docker Compose (recommended)

From the repository root:

```bash
docker compose up --build
```

- **PostgreSQL 16** starts first (healthcheck).
- **App** builds via multi-stage `Dockerfile`, connects with `SPRING_DATASOURCE_*` env vars.
- Liquibase migrates on startup.
- API: **http://localhost:8080**

### Local (without app container)

**Prerequisites:** Java 17, Maven, PostgreSQL 16+

```bash
# Database only
docker run -d --name order-engine-db \
  -e POSTGRES_USER=root \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=order_engine_db \
  -p 5432:5432 \
  postgres:16

./mvnw spring-boot:run
```

Config: `src/main/resources/application.yml` (defaults: `localhost:5432`, db `order_engine_db`, user `root` / `password`). Env vars override the datasource URL for Docker.

### API examples

**Calculate** (assignment scenario — subtotal **250**, final **102.50**):

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

| Discount type | Amount |
|---------------|--------|
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

**Create promotion**

```bash
curl -s -X POST http://localhost:8080/api/v1/promotions \
  -H "Content-Type: application/json" \
  -d '{ "type": "PERCENTAGE_DISCOUNT", "value": 10, "active": true }'
```

### Response envelope

All endpoints return:

```json
{
  "data": { },
  "error": null
}
```

Errors (e.g. invalid coupon) via `exception/GlobalException.java`:

```json
{
  "data": null,
  "error": {
    "message": "Coupon MISSING is not existed",
    "code": "COUPON_NOT_FOUND"
  }
}
```

---

## 7. How to run tests

**24 unit tests** — no Spring context; repositories mocked in service tests; real pipeline/strategies in domain tests.

```bash
./mvnw test
```

| Test | Location | Covers |
|------|----------|--------|
| `PercentageDiscountStrategyTest` | `domain/promotion/strategy/` | 10% off subtotal |
| `Buy2Get1StrategyTest` | `domain/promotion/strategy/` | Free units per SKU |
| `VipCustomerStrategyTest` | `domain/promotion/strategy/` | VIP 5% vs regular |
| `CouponStrategyTest` | `domain/promotion/strategy/` | Apply / missing / inactive / expired |
| `PromotionPipelineTest` | `domain/promotion/` | Full chain → 147.50 total discount |
| `PromotionServiceTest` | `api/service/` | Sum discounts, list/create (mocked repo) |
| `OrderServiceTest` | `api/service/` | End-to-end calculate, coupon errors (mocked repos) |
| `PromotionTestFixtures` | `support/` | Shared builders + pipeline factory |

Run one class:

```bash
./mvnw test -Dtest=OrderServiceTest
```

**Not yet implemented:** Testcontainers integration test (calculate against real PostgreSQL with seeded promotions).

---

## 8. Trade-offs and improvements

| Area | Current choice | With more time |
|------|----------------|----------------|
| **Discount stacking** | Percentage and VIP use **original subtotal**; discounts are additive, not applied to a running net. | Configurable stacking policies per rule. |
| **Buy 2 Get 1** | Always runs in pipeline; does not check `promotions.active` for `BUY2_GET1_FREE`. | Gate via `supports()` + DB flag like percentage. |
| **Order persistence** | `total_discount` column exists but `createOrder` does not set it on the entity. | Persist all calculated fields; align DB with API response. |
| **Coupons** | Unlimited reuse; no redemption table. | Single-use / per-customer limits with transactional redemption. |
| **Prices** | Line prices come from the request, not `products` table. | Resolve SKU → price from catalog with override rules. |
| **Pipeline `supports()`** | Defined on `PromotionHandler` but chain always calls `doHandle`. | Skip handlers when promotion type inactive in DB. |
| **Tests** | Strong unit coverage (strategies + services + pipeline). | Testcontainers test for `POST /calculate` against real DB. |
| **Final price floor** | No guard if discounts exceed subtotal. | Reject or cap at zero with clear error code. |

---

## 9. What breaks at scale and fixes

| Risk | Why it hurts | Mitigation |
|------|----------------|------------|
| **DB read per calculate** | Every request loads all active promotions + optional coupon. | Cache promotion catalog (Caffeine already on classpath); TTL + invalidation on admin writes. |
| **Order write amplification** | Each calculate inserts `orders` + `order_items`. | Async persistence, idempotency keys, or separate “quote” vs “commit” endpoints. |
| **Coupon races** | Same code usable concurrently without limits. | `coupon_redemptions` + unique constraints or `SELECT FOR UPDATE`. |
| **Hot `orders` table** | Append-only growth. | Partition by `created_at`, archive to warehouse, read replicas for reporting. |
| **Fixed pipeline order** | Rule sequence compiled into one JVM bean. | External config / per-tenant rule ordering. |
| **Stateless scaling** | Service is stateless today (safe for horizontal scale). | Run N replicas behind a load balancer; shared Postgres; optional distributed cache for promotions. |

**Concurrency note (Challenge 2):** Parallel `calculate` requests are safe for pricing math (per-request `PromotionContext`, stateless strategies). The main gap is **business-level** coupon reuse under load, not shared in-memory mutation.

---

## Project layout

```
order_engine/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── src/main/java/com/engine/order_engine/
│   ├── OrderEngineApplication.java
│   ├── api/
│   │   ├── controller/          # OrderController, PromotionController
│   │   ├── service/             # OrderService, PromotionService
│   │   └── dto/
│   │       ├── request/orders/  # CalculateOrderRequest
│   │       ├── request/promotion/
│   │       └── response/        # BaseResponse, BaseError, CreateOrderResponse
│   ├── domain/
│   │   ├── customer/            # CustomerType
│   │   ├── dto/                 # OrderItemRequest
│   │   ├── model/               # PromotionContext, PromotionDetail
│   │   └── promotion/
│   │       ├── chain/           # PromotionHandler, PromotionStrategyHandler
│   │       └── strategy/        # *Strategy implementations
│   ├── entity/                  # Order, OrderItem, Promotion, Coupon, Product
│   ├── repository/
│   ├── infrastructure/configuration/
│   └── exception/               # BusinessException, GlobalException
├── src/main/resources/
│   ├── application.yml
│   └── db/changelog/
└── src/test/java/com/engine/order_engine/
    ├── api/service/             # OrderServiceTest, PromotionServiceTest
    ├── domain/promotion/        # PromotionPipelineTest
    │   └── strategy/            # *StrategyTest
    └── support/                 # PromotionTestFixtures
```
