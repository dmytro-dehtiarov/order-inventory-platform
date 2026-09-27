package com.dmytro.orderinventoryplatform.inventory.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidQuantityExceptionTest {
    @Test
    public void testInvalidQuantityException() {
        String message = "Invalid quantity";
        InvalidQuantityException exception = new InvalidQuantityException(message);
        Assertions.assertEquals(message, exception.getMessage());
    }
}
