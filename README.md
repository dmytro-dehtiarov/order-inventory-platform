# Order & Inventory Management Platform

## Work in progress, actively building toward MVP

![CI](https://github.com/dmytro-dehtiarov/order-inventory-platform/actions/workflows/ci.yml/badge.svg)

Production-style Order & Inventory Management Platform built with **Java 17** and **Spring Boot**, designed to demonstrate real-world backend development practices including transactional business logic, layered architecture, and cloud-ready deployment.

## Project Goal
This project simulates a typical B2B backoffice system used to manage products, inventory, and orders.  
It is built as a **portfolio project** to prepare for **Software Engineer / Java Developer co-op and junior roles**.

The focus is on:
- clean architecture
- business logic (not just CRUD)
- testability
- production-like setup

---

## Architecture Overview
- **Architecture style:** Modular Monolith
- **Layers:**
  - API (Controllers, DTOs, Validation)
  - Application (Services / Use Cases)
  - Domain (Entities, Business Rules)
  - Infrastructure (Persistence, External integrations)

## Architecture Decisions
  - Key design decisions are documented as ADRs in [`docs/`](docs/).
 
## Key Features (MVP)

**Implemented:**
- Database schema and migrations (Flyway, Oracle)
- Domain entities for core modules (Catalog, Inventory, Orders)
- Centralized exception handling

**In Progress / Planned:**
- Identity & Security (JWT-based authentication, role-based access control: ADMIN, MANAGER)
- Catalog (product and category management)
- Inventory (stock tracking with reservation)
- Orders (lifecycle management: create, cancel; transactional consistency with inventory)
- REST API with validation and error handling
- Audit (later)
- Files & Reporting (later)

More details: [`docs/architecture.md`](docs/architecture.md)

---

## Tech Stack
- Java 17
- Spring Boot 4 (Web, Validation, Security)
- Oracle Database
- JPA / Hibernate
- Flyway (database migrations)
- Docker & Docker Compose
- Testing: JUnit 5, Mockito (integration tests against a dedicated Oracle test schema — see [`docs/architecture.md`](docs/architecture.md))
- CI/CD: Jenkins (planned)
- Cloud: AWS (planned – RDS, S3, DynamoDB)

---

## Getting Started (Local)

### Option A: Quick start with Docker (recommended)
Requirements: Java 17+, Docker

```bash
cp .env.example .env
# edit .env: set DB_HOST=localhost, DB_PORT=1521,
# DB_SERVICE=FREEPDB1, DB_USER and DB_PASSWORD of your choice

docker compose up -d
# wait ~60-90s for Oracle to initialize (check: docker compose logs -f oracle)

./mvnw spring-boot:run
```

### Option B: Connect to an existing Oracle instance
If you already have Oracle Database running (a college lab environment, a dedicated VM, a cloud
instance), skip Docker entirely:

```bash
cp .env.example .env
# fill in DB_HOST, DB_PORT, DB_SERVICE, DB_USER, DB_PASSWORD
# for your existing instance

./mvnw spring-boot:run
```

For a full from-scratch Oracle Linux VM setup (used during this project's development), see
[`docs/oracle-vm-setup.md`](docs/oracle-vm-setup.md).

