package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;

/**
 * Thrown when assigning a parent category would introduce a cycle in the
 * category hierarchy (a category becoming its own ancestor). Maps to HTTP 409
 * via {@link ConflictException}.
 */
public class CategoryCycleException extends ConflictException {

    /**
     * @param message a human-readable description of the cycle that was
     *                 detected
     */
    public CategoryCycleException(String message) {
        super(message);
    }
}
