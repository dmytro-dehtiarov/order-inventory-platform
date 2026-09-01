package com.dmytro.orderinventoryplatform.inventory.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InventoryItemNotFoundExceptionTest {
    @Test
    public void testInventoryItemNotFoundException() {
        String message = "inventory item not found";
        InventoryItemNotFoundException exception = new InventoryItemNotFoundException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }
}
