package com.dmytro.orderinventoryplatform.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Domain entity for stock tracking: one row per product, keyed by the
 * product's own id rather than a separate surrogate id (a shared
 * primary-key/foreign-key mapping), since the relationship to
 * {@code Product} is strictly one-to-one.
 *
 * <p>Deliberately holds no reference to {@code Product} itself - only its
 * {@code id} - to keep the {@code inventory} and {@code catalog} modules
 * independent (see architecture.md on module boundaries).
 *
 * <p>Enforces two invariants at all times: {@code available >= 0} and
 * {@code reserved >= 0}. All state changes go through {@link #reserve},
 * {@link #release}, or {@link #adjust}; there are deliberately no setters,
 * since exposing one would let a caller bypass these invariants.
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @Column(name = "product_id", nullable = false)
    private Long id;

    @Column(name = "available", nullable = false)
    private int available = 0;

    @Column(name = "reserved", nullable = false)
    private int reserved;

    protected InventoryItem() {}

    /**
     * @param id the id of the product this record tracks stock for
     * @param available the initial available quantity; must not be negative
     * @throws IllegalArgumentException if {@code available} is negative
     */
    public InventoryItem(Long id, int available) {
        this.id = id;
        if (available < 0) {
            throw new IllegalArgumentException("Available quantity cannot be negative");
        }
        this.available = available;
        this.reserved = 0;
    }

    /**
     * Moves {@code quantity} from {@code available} to {@code reserved},
     * for example when an order is created and its line items must be
     * held against future orders.
     *
     * @param quantity the quantity to reserve; must be positive
     * @throws IllegalArgumentException if {@code quantity} is not positive
     * @throws InsufficientStockException if {@code quantity} exceeds the
     *                                     currently available quantity
     */
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reserve must be more than zero");
        }
        if (quantity > available) {
            throw new InsufficientStockException("Not enough stock available to reserve");
        }
        available -= quantity;
        reserved += quantity;
    }

    /**
     * Moves {@code quantity} from {@code reserved} back to
     * {@code available}, the inverse of {@link #reserve} - for example
     * when an order is cancelled and its held stock must become available
     * again.
     *
     * @param quantity the quantity to release; must be positive
     * @throws IllegalArgumentException if {@code quantity} is not positive
     * @throws InsufficientStockException if {@code quantity} exceeds the
     *                                     currently reserved quantity
     */
    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to release must be more than zero");
        }
        if (quantity > reserved) {
            throw new InsufficientStockException("Cannot release more than reserved quantity");
        }
        available += quantity;
        reserved -= quantity;
    }

    /**
     * Directly corrects {@code available} to a new absolute value,
     * independent of {@code reserved} - for stock counts, restocks, or
     * write-offs, as opposed to {@link #reserve}/{@link #release}, which
     * are driven by order activity.
     *
     * @param quantity the new available quantity; must not be negative,
     *                  but may be zero (a legitimate out-of-stock count)
     * @throws IllegalArgumentException if {@code quantity} is negative
     */
    public void adjust(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity to adjust must not be negative");
        }
        available = quantity;
    }

    public Long getId() {
        return id;
    }

    public int getAvailable() {
        return available;
    }

    public int getReserved() {
        return reserved;
    }

}
