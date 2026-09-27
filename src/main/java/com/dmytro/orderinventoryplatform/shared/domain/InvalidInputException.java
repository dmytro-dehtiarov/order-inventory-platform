package com.dmytro.orderinventoryplatform.shared.domain;

/**
 * Base type for domain exceptions raised when the values supplied to an
 * operation are invalid in themselves, independently of the state of any
 * existing resource (for example, a negative quantity, or an order with no
 * line items).
 *
 * <p>Distinct from {@link ConflictException}: that one signals a request
 * that is well-formed but clashes with a resource's current state, such as
 * reserving more stock than is available. This one signals input that
 * would be wrong no matter what state the system is in.
 *
 * <p>Module-specific exceptions must extend this class rather than being
 * thrown directly, since this class is abstract. Domain entities throw
 * these to enforce their own invariants even when an API-layer check
 * should already have rejected the input, so the invariant holds for every
 * caller, not only for HTTP requests.
 *
 * <p>This class carries no knowledge of HTTP. The mapping to an HTTP status
 * code (400) happens in the global exception handler, which matches
 * exception handlers by this type.
 */
public abstract class InvalidInputException extends RuntimeException {
    /**
     * @param message a human-readable description of what was invalid
     */
    protected InvalidInputException(String message) {
        super(message);
    }
}
