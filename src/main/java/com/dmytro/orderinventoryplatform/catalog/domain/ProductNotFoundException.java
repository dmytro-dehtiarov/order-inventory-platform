package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;

/**
 * Thrown when a product is looked up by identifier and no such product
 * exists. Maps to HTTP 404 via {@link ResourceNotFoundException}.
 */
public class ProductNotFoundException extends ResourceNotFoundException {

    /**
     * @param message a human-readable description of which product could
     *                 not be found
     */
    public ProductNotFoundException(String message) {
        super(message);
    }
}
