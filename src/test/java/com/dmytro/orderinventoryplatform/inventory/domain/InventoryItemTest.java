package com.dmytro.orderinventoryplatform.inventory.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InventoryItemTest {
    private InventoryItem item;

    @BeforeEach
    void setUp() {
        item = new InventoryItem(1L, 10);
    }

    @Test
    public void shouldCreateInventoryItem() {
        Assertions.assertNotNull(item);
        Assertions.assertEquals(10, item.getAvailable());
        Assertions.assertEquals(0, item.getReserved());
    }

    @Test
    public void shouldReserveQuantity() {
        item.reserve(5);
        Assertions.assertEquals(5, item.getAvailable());
        Assertions.assertEquals(5, item.getReserved());
    }

    @Test
    public void shouldReleaseReserved() {
        item.reserve(5);
        item.release(3);
        Assertions.assertEquals(8, item.getAvailable());
        Assertions.assertEquals(2, item.getReserved());
    }

    @Test
    public void shouldAdjustAvailable() {
        item.adjust(15);
        Assertions.assertEquals(15, item.getAvailable());
        Assertions.assertEquals(0, item.getReserved());
    }

    @Test
    public void shouldAdjustToZero() {
        item.adjust(0);
        Assertions.assertEquals(0, item.getAvailable());
        Assertions.assertEquals(0, item.getReserved());
    }

    @Test
    public void shouldThrowWhenAvailableIsNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new InventoryItem(1L, -1));
    }

    @Test
    public void shouldThrowWhenReserveMoreThanAvailable() {
        Assertions.assertThrows(InsufficientStockException.class, () -> item.reserve(11));
    }

    @Test
    public void shouldThrowWhenReleaseMoreThanReserved() {
        item.reserve(5);
        Assertions.assertThrows(InsufficientStockException.class, () -> item.release(6));
    }

    @Test
    public void shouldThrowWhenReserveNegativeQuantity() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> item.reserve(-1));
    }

    @Test
    public void shouldThrowWhenReleaseNegativeQuantity() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> item.release(-1));
    }

    @Test
    public void shouldThrowWhenAdjustNegativeQuantity() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> item.adjust(-1));
    }
}
