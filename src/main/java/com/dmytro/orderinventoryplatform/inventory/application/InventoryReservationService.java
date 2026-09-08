package com.dmytro.orderinventoryplatform.inventory.application;

import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItemNotFoundException;
import com.dmytro.orderinventoryplatform.inventory.infrastructure.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service exposing the public reservation contract that other
 * modules (chiefly {@code orders}) call to reserve and release stock as
 * part of the order lifecycle.
 *
 * <p>Both methods use the default ({@code REQUIRED}) transaction
 * propagation: called on their own, they open their own transaction;
 * called from within an already-transactional use case (e.g.
 * {@code orders.OrderService.createOrder}), they join it instead, so the
 * reservation and the caller's other writes commit or roll back together.
 * Neither method re-validates the requested quantity against the available
 * stock itself, that invariant is enforced by {@link InventoryItem#reserve}
 * and {@link InventoryItem#release}.
 */
@Service
public class InventoryReservationService {
    private final InventoryItemRepository inventoryItemRepository;

    public InventoryReservationService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    /**
     * Reserves the given quantity against an inventory item's available
     * stock.
     *
     * <p>Locks the row with {@link InventoryItemRepository#lockedFindById}
     * for the duration of this transaction, so the read-reserve-save
     * sequence is atomic with respect to concurrent writers.
     *
     * @param productId the id of the product whose stock is reserved
     * @param quantity   the quantity to reserve
     * @return the updated inventory item
     * @throws InventoryItemNotFoundException if no inventory record exists
     *         for {@code productId}
     * @throws com.dmytro.orderinventoryplatform.inventory.domain.InsufficientStockException
     *         if {@code quantity} exceeds the available stock
     */
    @Transactional
    public InventoryItem reserveInventoryItem(Long productId, int quantity) {
        InventoryItem inventoryItem = inventoryItemRepository.lockedFindById(productId)
                .orElseThrow(() -> new InventoryItemNotFoundException(productId));
        inventoryItem.reserve(quantity);
        return inventoryItemRepository.save(inventoryItem);

    }

    /**
     * Releases a previously reserved quantity back into an inventory
     * item's available stock.
     *
     * <p>Locks the row with {@link InventoryItemRepository#lockedFindById}
     * for the duration of this transaction, so the read-release-save
     * sequence is atomic with respect to concurrent writers.
     *
     * @param productId the id of the product whose reservation is released
     * @param quantity   the quantity to release
     * @return the updated inventory item
     * @throws InventoryItemNotFoundException if no inventory record exists
     *         for {@code productId}
     * @throws com.dmytro.orderinventoryplatform.inventory.domain.InsufficientStockException
     *         if {@code quantity} exceeds the currently reserved quantity
     */
    @Transactional
    public InventoryItem releaseInventoryItem(Long productId, int quantity) {
        InventoryItem inventoryItem = inventoryItemRepository.lockedFindById(productId)
                .orElseThrow(() -> new InventoryItemNotFoundException(productId));
        inventoryItem.release(quantity);
        return inventoryItemRepository.save(inventoryItem);
    }
}
