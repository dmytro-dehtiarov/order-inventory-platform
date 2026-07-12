# ADR-003: Stateless JWT Authentication

## Status
Accepted

## Date
2026-07-12

## Context
ADR-001 deferred the authentication strategy to a follow-up decision. The identity module is now
designed (see `docs/design/full-architecture-plan.md`), so the mechanism must be fixed before
implementation. The system is a staff-facing REST API (roles `ADMIN` / `MANAGER`, no public
self-registration) served by a single Spring Boot deployable.

Key constraints and priorities:
- pure REST API with no server-rendered frontend — clients authenticate per request
- portfolio value: demonstrate the industry-standard token-based flow for REST APIs
- keep the modular monolith simple — no extra infrastructure (session store, identity provider)
- authorization is enforced inside the app: route rules in the security filter chain plus
  `@PreAuthorize` on application services ("authorization checks at the use-case level",
  `docs/architecture.md` §4)

## Decision
Use **stateless JWT bearer tokens**:
- `POST /api/v1/auth/login` (username + password, BCrypt-verified) issues a signed JWT carrying
  the user id and role
- every subsequent request presents `Authorization: Bearer <token>`; a security filter validates
  it and populates the `SecurityContext` — no server-side session state
- implementation via **Spring Security's `oauth2-resource-server`** (Nimbus) with a symmetric
  signing key supplied through environment variables (`.env` locally, per the ADR-002 config
  pattern) — no third-party JWT library (jjwt) needed
- new dependencies: `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server`

## Considered Options
1. **Stateless JWT (selected)**
2. **HTTP Basic** — credentials on every request
3. **Server-side sessions** — Spring Security session cookies

## Rationale
### Why JWT
- **Standard for REST APIs:** token-based auth is what API consumers expect and what the
  portfolio should demonstrate.
- **Stateless:** no session store to add or replicate; fits the single-deployable monolith and
  survives horizontal scaling unchanged.
- **Idiomatic Spring path:** `oauth2-resource-server` gives filter, validation, and
  `SecurityContext` integration out of the box — minimal custom security code, no extra
  third-party dependency.

### Why not HTTP Basic
- Sends the password on every request and forces BCrypt verification per call.
- Demonstrates no token handling — weaker portfolio signal.

### Why not sessions
- Cookie/session semantics fit browser frontends, not a pure REST API consumed via tokens.
- Adds server-side state for no benefit here.

## Consequences
### Positive
- No session infrastructure; authentication is self-contained in the identity module.
- Clean fit with the designed authorization model (filter chain + `@PreAuthorize`).

### Negative / Trade-offs
- Tokens cannot be revoked server-side before expiry (mitigation: short token lifetime; a refresh
  or blocklist mechanism is out of MVP scope).
- Signing key becomes a secret to manage via environment configuration.
- Slightly more moving parts than Basic (login endpoint, token issuance/validation).

## Follow-ups
- Decide token lifetime and whether a refresh-token flow is needed post-MVP.
- Key rotation strategy if/when the project moves to AWS.
