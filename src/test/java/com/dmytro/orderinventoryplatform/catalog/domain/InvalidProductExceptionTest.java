package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidProductExceptionTest {
    @Test
    public void testInvalidProductException() {
        String message = "Invalid product input";
        InvalidProductException exception = new InvalidProductException(message);
        Assertions.assertEquals(message, exception.getMessage());
    }
}
