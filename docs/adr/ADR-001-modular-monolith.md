# ADR-001: Choose a Modular Monolith Architecture

## Status
Accepted

## Date
2026-01-24

## Context
This project is a production-style portfolio backend built by a single engineer.
The goal is to demonstrate strong Java/Spring fundamentals, domain modeling,
transactional correctness, and a clean, maintainable architecture.

A microservices architecture was considered because it is common in many modern systems,
but it introduces additional complexity that may not provide proportional value for this project.

Key constraints and priorities:
- single-developer development and maintenance
- fast iteration and clear domain modeling
- simple deployment and debugging
- strong testability (unit + integration)
- ability to evolve architecture over time

## Decision
Adopt a **modular monolith** architecture:
- one deployable Spring Boot application
- clear internal module boundaries by domain (packages such as `orders`, `inventory`, `catalog`, `identity`)
- layered structure inside modules (API / application / domain / infrastructure)
- enforce module ownership (each module owns its business rules and data access)

## Considered Options
1. **Modular monolith (selected)**
2. **Microservices**
3. **Simple monolith without modular boundaries**

## Rationale
### Why modular monolith
- **Lower operational complexity:** single deployable artifact, no service discovery, no network failure modes.
- **Faster development:** fewer moving parts, easier to reason about changes.
- **Simpler debugging:** one process, one log stream (initially).
- **Better for learning and demonstration:** highlights core backend skills (Spring, SQL, transactions, testing).
- **Still scalable in structure:** module boundaries can later support extraction if needed.

### Why not microservices (for now)
- Requires additional infrastructure and operational concerns:
    - distributed logging/tracing
    - inter-service communication
    - versioning and deployment coordination
    - data ownership across services
- Increases time spent on DevOps rather than domain correctness.
- Harder to validate transactional invariants across services.

### Why not an unstructured monolith
- Higher risk of tight coupling and “ball of mud” growth.
- Harder to maintain and test as features expand.
- Less representative of professional engineering standards.

## Consequences
### Positive
- Clear, maintainable structure
- Easier testing and refactoring
- Simple local development and deployment
- Strong alignment with portfolio goals

### Negative / Trade-offs
- Single application can grow large if boundaries are not respected
- Some scalability patterns (independent deployments per module) are not available initially

## Follow-ups
- Add ADRs for other major decisions (e.g., database migrations tool, authentication strategy, audit storage).
- Maintain module boundaries and avoid cross-module data leaks.