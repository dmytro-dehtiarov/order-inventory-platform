package com.dmytro.orderinventoryplatform.inventory.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;

/**
 * Thrown when an inventory item is looked up by product id and no such
 * record exists. Maps to HTTP 404 via {@link ResourceNotFoundException}.
 */
public class InventoryItemNotFoundException extends ResourceNotFoundException {
    /**
     * @param productId the id of the product whose inventory record could
     *                   not be found
     */
    public InventoryItemNotFoundException(Long productId) {
        super("Inventory item not found for product ID: " + productId);
    }
}
