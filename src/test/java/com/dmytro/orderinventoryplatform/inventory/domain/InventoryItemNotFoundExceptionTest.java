package com.dmytro.orderinventoryplatform.inventory.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InventoryItemNotFoundExceptionTest {
    @Test
    public void testInventoryItemNotFoundException() {
        Long productId = 1L;
        InventoryItemNotFoundException exception = new InventoryItemNotFoundException(productId);

        Assertions.assertEquals("Inventory item not found for product ID: " + productId, exception.getMessage());
    }
}
