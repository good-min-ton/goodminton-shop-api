# Goodminton Shop API

E-commerce backend for a badminton equipment store. Spring Boot 3 · PostgreSQL · Redis · RabbitMQ · RAG chatbot.

---

## Tech Stack

| Layer              | Choice                                                                                |
| ------------------ | ------------------------------------------------------------------------------------- |
| Runtime            | Java 21, Spring Boot 3.5                                                              |
| Persistence        | PostgreSQL 15, Flyway, Spring Data JPA (Hibernate 6)                                  |
| Cache & session    | Redis (Spring Cache, JWT blacklist, chatbot memory)                                   |
| Auth               | JWT (Nimbus JOSE), Spring Security, OAuth2 Resource Server                            |
| Messaging          | RabbitMQ (product/order events for RAG sync)                                          |
| Search             | Postgres FTS (`unaccent` + `pg_trgm`), GIN indexes                                    |
| Payment            | PayOS (primary), VNPay (fallback)                                                     |
| Media              | Cloudinary (product images, category thumbnails, review media)                        |
| AI / chatbot       | RAG service (separate Python), Ollama (`bge-m3` embed, `qwen2.5:14b` LLM), pgvector   |
| Ops                | Docker Compose, Cloudflare / Tailscale Funnel, GitHub Actions CI/CD                   |

---

## System Architecture

```mermaid
flowchart LR
    FE[Frontend<br/>Next.js on Vercel] -->|HTTPS| API[Shop API<br/>Spring Boot]
    FE -.->|chat| RAG[RAG Service<br/>Python + Ollama]

    API --> PG[(PostgreSQL<br/>pgvector)]
    API --> REDIS[(Redis)]
    API --> MQ{{RabbitMQ}}
    API --> CLOUD[Cloudinary]
    API --> PAYOS[PayOS API]

    MQ --> RAG
    RAG --> PG
    RAG --> OLLAMA[Ollama<br/>bge-m3, qwen2.5]

    PAYOS -.->|webhook| API
```

- **Shop API** is the transactional core (products, orders, payment, auth).
- **RAG Service** is a separate service that consumes product/order events, updates `kb_chunks` in Postgres, and serves the chat endpoint.
- **RabbitMQ** decouples the two: shop-api publishes, rag-service consumes, so the main request path is never blocked by embedding work.
- **Ollama** is self-hosted on the VPS GPU. No dependency on OpenAI or paid inference APIs.

---

## Data Model (ERD)

A single ER diagram covering 15+ tables is unreadable, so it is split into three domain-focused sub-diagrams. The `resources` table is polymorphic (`owner_type` + `owner_id`, no FK) and attaches to products, product_variants, categories, and reviews — omitted from the diagrams for clarity.

### Identity and Stores

```mermaid
erDiagram
    accounts ||--o| stores : "manages (STORE_ADMIN)"
    accounts ||--o{ orders : places
    accounts ||--o{ reviews : writes

    accounts {
        int id PK
        string full_name
        string email UK
        string phone UK
        enum role "SUPER_ADMIN | STORE_ADMIN | CUSTOMER"
        enum status "ACTIVE | INACTIVE"
    }

    stores {
        int id PK
        int admin_id FK
        string name
        string address
        bool is_central
    }
```

### Catalog

```mermaid
erDiagram
    categories ||--o{ products : contains
    brands ||--o{ products : contains
    products ||--o{ products : "related (self-ref)"
    products ||--o{ product_specifications : has
    products ||--o{ product_variants : has
    colors ||--o{ product_variants : "color of"
    sizes ||--o{ product_variants : "size of"

    products {
        int id PK
        int category_id FK
        int brand_id FK
        int related_product_id FK "self-ref, nullable"
        string name
        string slug UK
        bool is_visible
    }

    product_variants {
        int id PK
        int product_id FK
        int color_id FK "nullable"
        int size_id FK "nullable"
        string sku_code UK
        decimal price
        decimal sale_price "nullable"
    }
```

### Commerce (orders, payments, inventory, reviews)

```mermaid
erDiagram
    stores ||--o{ inventory : holds
    product_variants ||--o{ inventory : "stocked in"

    accounts ||--o{ orders : places
    stores ||--o{ orders : fulfills
    orders ||--o{ order_items : contains
    orders ||--o{ payments : has
    product_variants ||--o{ order_items : "sold via"

    accounts ||--o{ reviews : writes
    products ||--o{ reviews : reviewed_by
    order_items ||--o| reviews : "reviewed once"

    orders {
        int id PK
        int customer_id FK "nullable (walk-in)"
        int store_id FK
        enum order_type "ONLINE | IN_STORE"
        enum status "PENDING | CONFIRMED | PREPARING | SHIPPING | DELIVERED | COMPLETED | CANCELLED"
        decimal total_amount
    }

    payments {
        int id PK
        int order_id FK
        enum method "COD | BANKING | VNPAY | PAYOS"
        enum status "PENDING | PAID | FAILED"
        decimal amount
        bigint payos_order_code "PayOS reference"
        string vnpay_txn_ref "VNPay reference"
    }
```

**Notable design choices:**

- `resources` is polymorphic (`owner_type` + `owner_id`, no FK) — one table serves product thumbnails, variant images, category thumbnails, and review media.
- `products.related_product_id` is a self-reference that links color/size siblings back to the root product.
- `orders.customer_id` is **nullable** so walk-in POS orders do not require a user account.
- `product_variants.color_id` and `size_id` are nullable for products with no meaningful color/size (shuttlecocks, bags).

Full schema: [db/migration/V1__init_schema.sql](src/main/resources/db/migration/V1__init_schema.sql)

---

## Core Flows

### 1. Online order and PayOS payment (happy path)

```mermaid
sequenceDiagram
    autonumber
    participant FE
    participant API as Shop API
    participant PG as Postgres
    participant PayOS
    participant Bank

    FE->>API: POST /api/orders (paymentMethod=PAYOS)
    API->>PG: Order(PENDING), assign central store, deduct stock (atomic)
    API-->>FE: OrderResponse

    FE->>API: POST /api/payos/create-payment-url
    API->>PayOS: create payment link (SDK)
    PayOS-->>API: {checkoutUrl, orderCode}
    API->>PG: Payment(PENDING, orderCode, linkId)
    API-->>FE: {paymentUrl}

    FE->>PayOS: window.location = paymentUrl
    Bank->>PayOS: user pays via QR
    par Server-to-server
        PayOS->>API: POST /api/payos/webhook
        API->>API: SDK.verify signature
        API->>PG: Payment=PAID, Order=CONFIRMED
        API-->>PayOS: 200 OK
    and Browser redirect
        PayOS->>FE: redirect returnUrl?code=00
        FE->>API: GET /api/orders/my/{id} (poll)
    end
```

### 2. RAG chatbot query

```mermaid
sequenceDiagram
    autonumber
    participant FE
    participant RAG as RAG Service
    participant PG as Postgres<br/>(pgvector)
    participant Ollama

    FE->>RAG: POST /chat { message, sessionId }
    RAG->>Ollama: embed(message) -> vector
    RAG->>PG: SELECT kb_chunks ORDER BY vector <-> :q LIMIT 5
    PG-->>RAG: top-5 context chunks
    RAG->>Ollama: LLM(system + context + history + question)
    Ollama-->>RAG: answer
    RAG-->>FE: { answer, sources }
```

### 3. Product sync (shop-api to rag-service via RabbitMQ)

```mermaid
sequenceDiagram
    autonumber
    participant Admin
    participant API as Shop API
    participant PG as Postgres
    participant MQ as RabbitMQ
    participant RAG as RAG Service
    participant Ollama

    Admin->>API: PUT /api/products/{id}
    API->>PG: update product (transactional)
    API->>API: publishAfterCommit(ProductChangedEvent)
    Note over API: publish only after the tx commits,<br/>otherwise a rollback would leave<br/>an orphan message downstream
    API->>MQ: publish product.updated

    MQ->>RAG: deliver (with retry + DLQ)
    RAG->>API: GET /api/products/{id} (fetch full data)
    API-->>RAG: ProductResponse
    RAG->>Ollama: embed(name + description + specs)
    Ollama-->>RAG: vector
    RAG->>PG: UPSERT kb_chunks WHERE source_type='product' AND source_id=:id
```

---

## Real cases

### A. Payment race condition (webhook vs browser redirect)

**Problem.** After the user pays, PayOS fires two events in parallel:
1. Server-to-server webhook to the backend, updating the DB.
2. Browser redirect to the frontend result page.

The webhook usually arrives first, but that ordering is not guaranteed.

```mermaid
sequenceDiagram
    participant User
    participant FE
    participant API
    participant PayOS

    Note over User,PayOS: User completes payment
    par Race
        PayOS->>API: webhook (~500ms)
        API->>API: Payment=PAID
    and
        PayOS->>User: 302 redirect (~2s)
        User->>FE: /payment/result?code=00
        FE->>API: GET /api/orders/my/{id}
    end
    Note over FE: If the API still returns PENDING<br/>(webhook not yet processed)<br/>the FE polls every 2s
    loop until CONFIRMED or 10s
        FE->>API: GET /api/orders/my/{id}
        API-->>FE: status
    end
```

**Solution.** The FE polls `/api/orders/my/{id}` every 2 seconds, up to 10 seconds.

### B. Webhook idempotency (PayOS retry)

**Problem.** PayOS retries the webhook until it receives `200 OK`, so the same payment may be delivered 2 or 3 times.

```mermaid
flowchart TD
    A[Webhook incoming] --> B{Signature valid?}
    B -->|No| E[return 200 + Invalid signature]
    B -->|Yes| C{Payment exists?}
    C -->|No| F[return 200 + Order not found]
    C -->|Yes| D{status == PENDING?}
    D -->|No, already PAID| G[return 200 + already processed]
    D -->|Yes| H[Amount match?]
    H -->|No| I[return 200 + Invalid amount]
    H -->|Yes| J[Update PAID, flip Order CONFIRMED]
    J --> K[return 200 + Confirmed]
```

**Key rule.** Always respond with 200 so PayOS stops retrying. Guarding on `status != PENDING` before updating makes the handler idempotent — repeated deliveries hit the "already processed" branch.

Code: [PayOSServiceImpl.processWebhook](src/main/java/com/lezh1n/goodminton_shop_api/services/impl/PayOSServiceImpl.java)

### C. Atomic stock decrement (concurrent purchase race)

**Problem.** Two customers click "Buy" when only one unit is left. Both requests read `quantity = 1`, both think stock is available, both write `quantity = 0` — double sell.

Naive approach (broken):

```java
Inventory inv = repo.findByStoreAndVariant(sid, vid);
if (inv.getQuantity() >= qty) {          // race condition here
    inv.setQuantity(inv.getQuantity() - qty);
    repo.save(inv);
}
```

Correct approach — a single atomic `UPDATE ... WHERE quantity >= :qty` at the DB level:

```mermaid
flowchart LR
    A[deduct request] --> B["UPDATE inventory<br/>SET quantity = quantity - :qty<br/>WHERE store=:sid AND variant=:vid<br/>AND quantity >= :qty"]
    B --> C{updated<br/>rows?}
    C -->|1| D[Success]
    C -->|0| E{"inventory row<br/>exists?"}
    E -->|Yes| F[INSUFFICIENT_STOCK]
    E -->|No| G[NOT_FOUND]
```

Postgres serialises the `UPDATE ... WHERE` with a row-level lock, so concurrent updates cannot both succeed.

Code: [InventoryRepository.decrementIfAvailable](src/main/java/com/lezh1n/goodminton_shop_api/repositories/InventoryRepository.java)

### D. Order expiration (VNPay / PayOS pending without payment)

**Problem.** The user creates an order, chooses PAYOS, then abandons the checkout. The order stays PENDING forever and the deducted stock is never released.

```mermaid
flowchart LR
    A[User creates order<br/>Stock deducted] --> B[Payment PENDING]
    B --> C{Within 15 min}
    C -->|Paid| D[Webhook -> CONFIRMED]
    C -->|No payment| E[Scheduler<br/>every 5 min]
    E --> F[Payment=FAILED<br/>Order=CANCELLED<br/>Stock restocked]
```

`cancelExpiredProviderPaymentOrders` runs every 5 minutes and covers both VNPay and PayOS. Timeout is configurable per provider via `payment-timeout-minutes`.

Code: [OrderScheduler](src/main/java/com/lezh1n/goodminton_shop_api/services/impl/OrderScheduler.java)

### E. Recommendation cache eviction

Recommendations are `@Cacheable` with a 2h TTL. A few events must invalidate the cache immediately:

```mermaid
flowchart LR
    A[Order COMPLETED] --> B["@CacheEvict<br/>(RECOMMENDATIONS, allEntries=true)"]
    C[Product updated] --> B
    D[Auto-complete<br/>scheduler] --> B
```

Trade-off: `allEntries=true` flushes the whole cache rather than a per-product entry — with ~1k products, this is simpler than selective eviction and the recompute cost is negligible.

### F. Rate limiter (not implemented, recommended)

Not currently implemented. Recommended for sensitive endpoints:

```mermaid
flowchart LR
    R[Request] --> RL{Rate limiter<br/>Bucket4j + Redis}
    RL -->|OK| API[Endpoint]
    RL -->|Exceeded| RESP[429 Too Many]

    subgraph Suggested limits
        L1["/api/auth/login<br/>5 req / 5 min / IP"]
        L2["/api/vnpay/webhook<br/>100 req / min / IP"]
        L3["/api/search<br/>60 req / min / user"]
    end
```

Suggested library: `bucket4j-spring-boot-starter` with Redis-backed distributed state.

### G. Related patterns

| Pattern                                                             | Where                              | Purpose                                                       |
| ------------------------------------------------------------------- | ---------------------------------- | ------------------------------------------------------------- |
| Envelope `ApiResponse<T>`                                           | every endpoint                     | Unified error/success handler on the FE                       |
| `@ControllerAdvice` GlobalExceptionHandler                          | exceptions                         | Maps `AppException` to HTTP status + business code            |
| Ownership check at the service layer                                | inventory, order, review           | STORE_ADMIN can only touch orders/inventory of its own store  |
| Cloudinary cleanup via `TransactionSynchronization.afterCommit`     | resource upload                    | Never delete a cloud file if the transaction rolls back       |
| `@BatchSize(50)` on Order collections                               | orderItems, payments               | Avoids `MultipleBagFetchException` and N+1 loads              |
| Polymorphic resources                                               | resources table                    | One table serves many owner types                             |


---

## Getting started

### Prerequisites
- Docker + Docker Compose
- 8GB RAM minimum (Ollama `qwen2.5:14b` needs roughly 10GB VRAM/RAM)

### Setup

```bash
cp .env.example .env
# Fill in credentials: PAYOS_*, CLOUDINARY_*, POSTGRES_*, JWT_SECRET

docker compose -f docker-compose.dev.yml up -d --build
```

Endpoints:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Migrations
Flyway runs automatically on startup. Files live in `src/main/resources/db/migration/V*.sql`.

### Documentation
Runtime API docs are available at Swagger UI once the server is up (`/swagger-ui.html`). Additional integration notes are kept out of the public repository.

---

## CI/CD Pipeline

Two GitHub Actions workflows, both defined under `.github/workflows/`.

```mermaid
flowchart LR
    PR[Pull request] --> CI
    PUSH[Push to main] --> CI
    PUSH --> CD

    subgraph CI[ci.yml - Build and Test]
        direction TB
        CI1[Checkout] --> CI2[Setup JDK 21]
        CI2 --> CI3[Start Postgres pgvector service]
        CI3 --> CI4[mvn verify]
    end

    subgraph CD[cd.yml - Build and Deploy]
        direction TB
        B1[Checkout] --> B2[Setup JDK 21 + Maven cache]
        B2 --> B3[mvn clean package -DskipTests]
        B3 --> B4[Docker build]
        B4 --> B5[Push image:latest and image:sha to Docker Hub]
        B5 --> D1[self-hosted runner on VPS]
        D1 --> D2[docker compose pull shop-api]
        D2 --> D3[docker compose up -d --no-deps shop-api]
        D3 --> D4[docker image prune -f]
    end
```

- **CI** (`ci.yml`) runs on every PR and every push to `main`. It boots a `pgvector/pgvector:pg15` service container so integration tests can hit a real Postgres instance.
- **CD** (`cd.yml`) runs only on push to `main`. The `build` job packages the app, builds a Docker image tagged with both `latest` and the commit SHA, and pushes to Docker Hub. The `deploy` job runs on a self-hosted runner living on the production VPS — it pulls the new image and restarts only the `shop-api` service, leaving Postgres, Redis, RabbitMQ, Ollama, and RAG containers untouched.
- **Rollback** — every image is tagged with its commit SHA, so a rollback is `docker compose … pull shop-api@sha256:… && up -d`. In practice, redeploy the previous SHA tag.
- **Secrets** — `DOCKER_USERNAME`, `DOCKER_PASSWORD` configured at the repo level. VPS credentials live on the self-hosted runner, not in GitHub.

---

## License

Private project — for portfolio and educational purposes.
