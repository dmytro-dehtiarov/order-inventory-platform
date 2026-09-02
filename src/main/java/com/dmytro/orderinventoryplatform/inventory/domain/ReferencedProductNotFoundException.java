package com.dmytro.orderinventoryplatform.inventory.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;

/**
 * Thrown when an operation references a product by id that does not exist
 * in the {@code catalog} module - for example, creating an inventory item
 * for a {@code productId} with no matching row in {@code products}.
 *
 * <p>Detected indirectly, via the {@code products} foreign key constraint
 * on {@code inventory_items.product_id} rather than a direct lookup, since
 * {@code inventory} does not depend on {@code catalog}'s repositories (see
 * architecture.md on module boundaries). Maps to HTTP 404 via
 * {@link ResourceNotFoundException}.
 */
public class ReferencedProductNotFoundException extends ResourceNotFoundException {
    /**
     * @param productId the id that was referenced but does not correspond
     *                   to any existing product
     */
    public ReferencedProductNotFoundException(Long productId) {
        super("Referenced product with ID " + productId + " not found.");
    }
}
