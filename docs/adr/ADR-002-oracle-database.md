# ADR-002: Use Oracle Database as the Primary Datastore

## Status
Accepted

## Date
2026-07-10

## Context
The project initially planned to use PostgreSQL as the primary transactional datastore (see
ADR-001 and the original `docs/architecture.md`). Since then, two things changed:

- Oracle Database is the current subject of coursework at college, so building the project
  against it reinforces what's being learned in class.
- An Oracle VM is already provisioned and available, which removes the need to run and maintain
  a local PostgreSQL container (or pay for a managed Postgres instance later) — lowering
  infrastructure cost and setup effort for a single-developer project.

Key constraints and priorities:
- reuse already-available infrastructure (the Oracle VM) instead of standing up a new database
- align the project with current coursework to reinforce learning
- keep local developer setup simple (connection details via environment variables, not hardcoded)
- preserve the existing modular monolith architecture and layering (ADR-001) — this change only
  affects the infrastructure layer's persistence technology

## Decision
Migrate the primary datastore from PostgreSQL to **Oracle Database**:
- JDBC connectivity via `ojdbc11`
- schema migrations via `flyway-database-oracle`
- Hibernate dialect: `org.hibernate.dialect.OracleDialect`
- datasource connection details (host, port, service name, credentials) are supplied via
  environment variables, loaded locally from a `.env` file (via `spring-dotenv`) and never
  committed to source control — only `.env.example` (a template with no real values) is tracked
- application connects to a schema on the already-provisioned Oracle VM rather than a local
  containerized database

## Considered Options
1. **Oracle Database (selected)**
2. **Keep PostgreSQL** (as originally decided in ADR-001)
3. **MySQL / MariaDB**

## Rationale
### Why Oracle Database
- **Reuses existing infrastructure:** the Oracle VM is already deployed, so there's no additional
  cost or setup to stand up a new database compared to running PostgreSQL locally via Docker.
- **Aligned with current learning:** college coursework is currently focused on Oracle, so using
  it here reinforces those skills in a production-style project instead of a disconnected one.
- **Still a mature, standards-compliant relational database:** supports the same transactional
  guarantees the project's domain invariants depend on (atomic inventory reserve/release, etc.).

### Why not keep PostgreSQL
- Would require maintaining a separate local Postgres container (or hosted instance) even though
  a suitable Oracle VM is already available and idle.
- Doesn't reinforce current coursework.

### Why not MySQL/MariaDB
- No existing infrastructure or coursework alignment advantage over Oracle.
- Would still require standing up new infrastructure, same as staying on PostgreSQL.

## Consequences
### Positive
- No new infrastructure to provision or pay for — the existing Oracle VM is reused.
- Direct reinforcement of Oracle skills being learned concurrently in college.
- Connection configuration is externalized via environment variables from day one, avoiding
  hardcoded credentials in source control.

### Negative / Trade-offs
- **Heavier integration tests:** the Testcontainers Oracle image is significantly larger and
  slower to pull/start than `postgres:alpine`, which was the original plan under ADR-001.
- **Integration tests will not run against a disposable Testcontainers instance.** Instead, they
  run against a dedicated test schema on the same Oracle VM used for local development. This
  means test isolation must be managed manually (e.g., schema resets between runs) and tests are
  not fully hermetic/portable the way a spun-up-per-run container would be.
- Oracle-specific SQL dialect and Flyway migration syntax reduce portability if the project ever
  needs to move to a different relational database.
- Local development now has an external dependency (the Oracle VM must be reachable) rather than
  a fully self-contained `docker compose up`.

## Follow-ups
- Document the Oracle VM connection setup and the test schema provisioning steps for onboarding.
- Revisit test isolation strategy for the shared test schema (e.g., transactional rollback per
  test, or a schema-reset script) since Testcontainers-per-run isolation is no longer available.
