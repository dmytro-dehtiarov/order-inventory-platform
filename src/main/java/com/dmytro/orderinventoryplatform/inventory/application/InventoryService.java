package com.dmytro.orderinventoryplatform.inventory.application;

import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItemNotFoundException;
import com.dmytro.orderinventoryplatform.inventory.domain.ReferencedProductNotFoundException;
import com.dmytro.orderinventoryplatform.inventory.infrastructure.InventoryItemRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for the CRUD-style inventory use cases: creating an
 * inventory record for a product, reading it, and correcting its stock
 * count (stocktaking). Reservation/release for the order lifecycle is
 * handled separately by {@code InventoryReservationService}.
 */
@Service
public class InventoryService {
    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * Creates an inventory record for an existing product, with the given
     * starting stock count.
     *
     * <p>Relies on the {@code product_id} foreign key constraint on
     * {@code inventory_items} to validate that the product exists, rather
     * than a direct cross-module lookup (see {@code architecture.md} on
     * module boundaries): a violation is translated into
     * {@link ReferencedProductNotFoundException}.
     *
     * @param productId the id of the product this record is created for,
     *                   reused as the inventory item's own primary key
     * @param quantity   the starting available quantity
     * @return the created inventory item
     * @throws ReferencedProductNotFoundException if no product with
     *         {@code productId} exists
     */
    public InventoryItem createInventoryItem(Long productId, int quantity) {
        InventoryItem inventoryItem = new InventoryItem(productId, quantity);
        try  {
            inventoryItemRepository.save(inventoryItem);
        } catch (DataIntegrityViolationException e) {
            throw new ReferencedProductNotFoundException(productId);
        }
        return inventoryItem;
    }

    /**
     * Looks up the inventory record for a product.
     *
     * <p>Uses the plain, non-locking read, since this is a read-only view
     * of stock levels rather than a write path.
     *
     * @param productId the id of the product whose inventory is requested
     * @return the inventory item
     * @throws InventoryItemNotFoundException if no inventory record exists
     *         for {@code productId}
     */
    public InventoryItem getInventoryItem(Long productId) {
        return inventoryItemRepository.findById(productId)
                .orElseThrow(() -> new InventoryItemNotFoundException(productId));
    }

    /**
     * Corrects an inventory item's available quantity to an absolute value,
     * for example after a physical stocktake.
     *
     * <p>Locks the row with {@link InventoryItemRepository#lockedFindById}
     * for the duration of this transaction, so the read-adjust-save
     * sequence is atomic with respect to concurrent writers.
     *
     * @param productId the id of the product whose inventory is adjusted
     * @param quantity   the new absolute available quantity
     * @return the updated inventory item
     * @throws InventoryItemNotFoundException if no inventory record exists
     *         for {@code productId}
     */
    @Transactional
    public InventoryItem adjustInventoryItem(Long productId, int quantity) {
        InventoryItem inventoryItem = inventoryItemRepository.lockedFindById(productId)
                .orElseThrow(() -> new InventoryItemNotFoundException(productId));
        inventoryItem.adjust(quantity);
        return inventoryItemRepository.save(inventoryItem);
    }
}
