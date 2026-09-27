# ADR-004: Base Domain Exception for Invalid Input

## Status
Accepted

## Date
2026-09-27

## Context
Domain entities enforce their own invariants and throw when one is violated, independently of any
validation performed at the API boundary (`docs/architecture.md` §4). Bean Validation on request
DTOs rejects malformed input before it reaches the domain, but the domain check must still hold,
because entities are invoked from paths that never pass through a controller: unit tests, calls
between application services, and cross-module calls such as `orders` invoking
`inventory.InventoryReservationService` directly as an in-process call (ADR-001).

Until now `shared.domain` provided only two base exception types, both mapped in
`GlobalExceptionHandler`:
- `ResourceNotFoundException` to 404
- `ConflictException` to 409

Invariant violations had no matching base type and threw `IllegalArgumentException`, which the
handler does not map. A violation escaping to a controller therefore produced **500 Internal
Server Error**, reporting a caller's bad input as a server fault. Fourteen such call sites existed
across `catalog` (`Category`, `Product`) and `inventory` (`InventoryItem`).

## Decision
Add a third base type, `InvalidInputException`, to `shared.domain` and map it to a 400
`ProblemDetail` in `GlobalExceptionHandler`.

It mirrors the two existing base types exactly: abstract, `protected` constructor taking a
message, extending `RuntimeException`, and carrying no knowledge of HTTP. Module-specific
exceptions extend it rather than being thrown directly.

Migrate every existing domain invariant check off `IllegalArgumentException`:
- `inventory`: `InvalidQuantityException` (negative initial stock, non-positive reserve,
  non-positive release, negative adjustment)
- `catalog`: `InvalidCategoryException` and `InvalidProductException`, following the module's
  existing per-entity convention (`CategoryNotFoundException`, `ProductNotFoundException`)

All three take a message, since the violations they cover differ in kind and no caller needs to
distinguish them programmatically.

## Considered Options
1. **New base type mapped to 400 (selected)**
2. **Map `IllegalArgumentException` globally** in `GlobalExceptionHandler`
3. **Leave as is**, relying on Bean Validation to make the domain check unreachable

## Rationale
### Why a dedicated base type
The mapping becomes intentional: only exceptions deliberately defined as "the caller's fault"
produce a 400. The boundary between client error and server fault stays meaningful, and the three
base types now cover the handler's full surface uniformly.

### Why not map `IllegalArgumentException`
It is a one-line change, and it was rejected for one reason: this type is not owned by the
application. The JDK and libraries throw it constantly, including `Enum.valueOf` on an unknown
constant, `UUID.fromString` on a malformed string, and various Spring internals. Mapping it
globally would report a genuine server bug to the client as a 400.

That is not a cosmetic concern. The 4xx/5xx split is what alerting and error budgets are built on.
Server bugs silently reclassified as client errors leave the service looking healthy while it is
broken, and the failure surfaces through user reports instead of monitoring.

### Why not leave as is
Bean Validation only guards the HTTP boundary. The domain is reached by other callers, and the
invariant must behave the same for all of them. Keeping a code path whose known outcome is a
misleading 500 is a latent defect, not an acceptable trade.

## Consequences
### Positive
- Domain invariant violations produce 400 from any caller, not only through the API layer.
- `IllegalArgumentException` no longer appears anywhere in the codebase as an invariant signal, so
  there is one convention rather than two.
- New modules have a clear pattern to follow.

### Negative / Trade-offs
- A one-time refactor of 14 call sites plus their tests across two modules.
- One more base type to choose between when adding an exception. The rule: `InvalidInputException`
  when the supplied values are wrong in themselves, `ConflictException` when the request is
  well-formed but clashes with a resource's current state.
- Every module now needs its own invalid-input subclass rather than reaching for a JDK type.

## Follow-ups
- `orders` and `identity` must follow this pattern as they are implemented.
