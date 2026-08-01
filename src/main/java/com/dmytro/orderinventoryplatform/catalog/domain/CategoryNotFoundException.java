package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;

/**
 * Thrown when a category is looked up by identifier and no such category
 * exists. Maps to HTTP 404 via {@link ResourceNotFoundException}.
 */
public class CategoryNotFoundException extends ResourceNotFoundException {

    /**
     * @param message a human-readable description of which category could
     *                 not be found
     */
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
