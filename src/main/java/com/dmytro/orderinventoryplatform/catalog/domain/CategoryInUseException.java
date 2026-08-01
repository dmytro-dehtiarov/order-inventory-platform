package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;

/**
 * Thrown when a category cannot be deleted because it is still referenced by
 * child categories or products. Maps to HTTP 409 via {@link ConflictException}.
 */
public class CategoryInUseException extends ConflictException {

    /**
     * @param message a human-readable description of which category is
     *                 still in use and why it cannot be deleted
     */
    public CategoryInUseException(String message) {
        super(message);
    }
}
