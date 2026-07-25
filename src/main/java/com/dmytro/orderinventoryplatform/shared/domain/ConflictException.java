package com.dmytro.orderinventoryplatform.shared.domain;
/**
 * Base type for domain exceptions raised when a requested operation
 * conflicts with the current state of a resource, violating a domain
 * invariant.
 *
 * <p>Module-specific exceptions (for example, reserving more inventory than
 * is available) must extend this class rather than being thrown directly,
 * since this class is abstract.
 *
 * <p>This class carries no knowledge of HTTP. The mapping to an HTTP status
 * code (409) happens in the global exception handler, which matches
 * exception handlers by this type.
 */
public abstract class ConflictException extends RuntimeException {
    /**
     * @param message a human-readable description of the conflict
     */
    protected ConflictException(String message) {
        super(message);
    }
}
