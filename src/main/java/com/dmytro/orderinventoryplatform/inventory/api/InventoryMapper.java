package com.dmytro.orderinventoryplatform.inventory.api;

import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link InventoryItem} domain entity and its API
 * representation.
 */
@Component
public class InventoryMapper {
    /**
     * Builds the API response for an inventory item, computing the
     * display-only {@code totalQuantity} field from the domain object's
     * available and reserved quantities.
     *
     * @param inventoryItem the domain entity to map
     * @return the corresponding API response
     */
    public InventoryResponse toResponse(InventoryItem inventoryItem) {
        return new InventoryResponse(
                inventoryItem.getId(),
                inventoryItem.getAvailable(),
                inventoryItem.getReserved(),
                inventoryItem.getAvailable() + inventoryItem.getReserved()
        );
    }
}
