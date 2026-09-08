package com.dmytro.orderinventoryplatform.inventory.api;

/**
 * API representation of an inventory item's stock levels.
 *
 * @param id               the product's id, reused as the inventory
 *                         item's own primary key
 * @param availableQuantity the quantity currently available to reserve
 * @param reservedQuantity  the quantity currently held against orders
 * @param totalQuantity     {@code availableQuantity + reservedQuantity};
 *                          a display-only value with no domain meaning of
 *                          its own
 */
public record InventoryResponse(
        Long id,
        int availableQuantity,
        int reservedQuantity,
        int totalQuantity
) {
}
