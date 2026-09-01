package com.dmytro.orderinventoryplatform.inventory.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InsufficientStockExceptionTest {
    @Test
    public void testInsufficientStockException() {
        String message = "insufficient stock";
        InsufficientStockException exception = new InsufficientStockException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }
}
