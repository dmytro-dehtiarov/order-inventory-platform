package com.dmytro.orderinventoryplatform.inventory.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;

/**
 * Thrown when an operation on an {@link InventoryItem} would violate its
 * stock invariants - reserving more than is available, or releasing more
 * than is currently reserved. Maps to HTTP 409 via {@link ConflictException}.
 */
public class InsufficientStockException extends ConflictException {
    /**
     * @param message a human-readable description of which quantity could
     *                 not be reserved or released, and why
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}
