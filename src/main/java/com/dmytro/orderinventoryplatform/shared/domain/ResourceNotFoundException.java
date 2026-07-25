package com.dmytro.orderinventoryplatform.shared.domain;
/**
 * Base type for domain exceptions raised when a requested resource does not
 * exist.
 *
 * <p>Module-specific exceptions (for example, a missing product in the
 * catalog module or a missing order in the orders module) must extend this
 * class rather than being thrown directly, since this class is abstract.
 *
 * <p>This class carries no knowledge of HTTP. The mapping to an HTTP status
 * code (404) happens in the global exception handler, which matches
 * exception handlers by this type.
 */
public abstract class ResourceNotFoundException extends RuntimeException {
    /**
     * @param message a human-readable description of which resource could
     *                 not be found
     */
    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
