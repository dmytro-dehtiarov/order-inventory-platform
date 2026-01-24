# Architecture Overview

## 1) Purpose
This document describes the high-level architecture of the **Order & Inventory Management Platform**.

The project is intentionally built to resemble real-world backend development:
- clear module boundaries
- layered design
- transactional business logic
- testability and maintainability
- cloud-ready structure (later phases)

---

## 2) Architectural Style: Modular Monolith
The system is implemented as a **modular monolith**:
- one deployable application (single Spring Boot service)
- internal modules organized by domain ownership

### Why modular monolith (instead of microservices)
Microservices introduce operational complexity (distributed tracing, service discovery, network failures, deployment orchestration).
For a portfolio project and single-developer setup, a modular monolith allows:
- faster delivery
- simpler deployment
- easier debugging
- strong domain modeling and clean boundaries

If needed, modules can later be extracted into separate services.

---

## 3) High-Level Modules (Domain Ownership)
Planned internal modules (package boundaries):

- **identity**: authentication, authorization, roles
- **catalog**: products, categories
- **inventory**: stock tracking, reservation logic
- **orders**: order lifecycle and business rules
- **audit** (later): audit events, event log (DynamoDB)
- **files** (later): file storage (S3), exports/invoices
- **reporting** (later): SQL-based reports and aggregates
- **shared**: cross-cutting utilities (errors, pagination, tracing)

Principle: **each module owns its data and business rules**.

---

## 4) Layered Architecture
The application follows a layered design:

### API Layer
Responsibilities:
- REST controllers (Spring MVC)
- request/response DTOs
- validation (Bean Validation)
- mapping DTOs <-> domain models
- consistent error responses

### Application Layer
Responsibilities:
- use cases / services (business orchestration)
- transactional boundaries (`@Transactional`)
- authorization checks at the use-case level
- integration coordination (e.g., persistence + events)

### Domain Layer
Responsibilities:
- entities and value objects
- invariants (what must always be true)
- state transitions (e.g., order status flow)

### Infrastructure Layer
Responsibilities:
- JPA repositories (Hibernate)
- database migrations (Flyway)
- external integrations (AWS clients later)
- configuration (profiles: local/test/aws)

---

## 5) Core Domain Concepts (MVP)
### Catalog
- Product
- Category

### Inventory
- InventoryItem (tracks `available` and `reserved`)
- reservation rules are enforced by application/domain logic

### Orders
- Order
- OrderItem
- status lifecycle (MVP):
  - `CREATED` -> `CANCELLED`
  - (later: `PAID`, `SHIPPED`, `COMPLETED`)

---

## 6) Data Storage Strategy
### PostgreSQL (primary)
Used for transactional consistency:
- users / roles
- products / categories
- inventory
- orders / order_items

### DynamoDB (planned)
Used for audit/event logging:
- audit events with time-based sorting
- immutable event records
- supports scalability and fast lookup by aggregate

### S3 (planned)
Used for file storage:
- exports (CSV)
- invoices (PDF)
- attachments

---

## 7) Transaction and Consistency Rules (Key Invariants)
MVP invariants:
- inventory `available >= 0`
- inventory `reserved >= 0`
- creating an order must **reserve** inventory atomically
- cancelling an order must **release** reserved inventory atomically
- order creation and inventory reservation happen in a single transaction

---

## 8) API Conventions
- Base path: `/api/v1`
- DTO validation on inputs
- consistent error shape (problem-like response)
- pagination/sorting for list endpoints

Documentation is provided via Swagger/OpenAPI.

---

## 9) Testing Strategy
- **Unit tests** for business rules (services/use-cases)
- **Integration tests** with Testcontainers (PostgreSQL)
- focus on verifying invariants (reserve/release logic, status transitions)

---

## 10) Deployment (Planned)
- local: Docker Compose (app + postgres)
- CI/CD: Jenkins pipeline (build/test/package/docker)
- cloud: AWS (RDS Postgres, ECS/EC2, S3, DynamoDB)

---

## 11) Architecture Decision Records
Key decisions are tracked in:
- `docs/adr/`

Example:
- ADR-001: Modular Monolith architecture
