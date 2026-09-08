package com.dmytro.orderinventoryplatform.inventory.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for correcting an inventory item's available quantity to an
 * absolute value, for example after a physical stocktake.
 *
 * <p>The product is identified by the {@code productId} path variable on
 * the endpoint, not by a field here.
 *
 * @param quantity the new absolute available quantity; must not be negative
 */
public record InventoryAdjustmentRequest (
        @NotNull @DecimalMin(value = "0", inclusive = true) Integer quantity
) {
}
