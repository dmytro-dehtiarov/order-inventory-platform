package com.dmytro.orderinventoryplatform.inventory.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating an inventory record for an existing product.
 *
 * @param productId the id of the product this record is created for; the
 *                   product must already exist in the {@code catalog}
 *                   module
 * @param quantity   the starting available quantity; must not be negative
 */
public record InventoryItemCreateRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0", inclusive = true) Integer quantity
) {
}
