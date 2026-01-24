# Order & Inventory Management Platform

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
 
## Key Features (MVP)
- Product and category management
- Inventory tracking with stock reservation
- Order lifecycle management (create, cancel)
- Transactional consistency between orders and inventory
- Role-based access (ADMIN, MANAGER)
- REST API with validation and error handling

Planned modules:
- Identity & Security
- Catalog
- Inventory
- Orders
- Audit (later)
- Files & Reporting (later)

More details: [`docs/architecture.md`](docs/architecture.md)

---

## Tech Stack
- Java 17
- Spring Boot 3 (Web, Validation, Security)
- PostgreSQL
- JPA / Hibernate
- Flyway (database migrations)
- Docker & Docker Compose
- Testing: JUnit 5, Mockito, Testcontainers
- CI/CD: Jenkins (planned)
- Cloud: AWS (planned – RDS, S3, DynamoDB)

---

## Getting Started (Local)
Requirements:
- Java 17+
- Docker

```bash
docker compose up --build
