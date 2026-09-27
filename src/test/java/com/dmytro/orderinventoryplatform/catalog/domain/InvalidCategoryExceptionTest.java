package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidCategoryExceptionTest {
    @Test
    public void testInvalidCategoryException() {
        String message = "Invalid category input";
        InvalidCategoryException exception = new InvalidCategoryException(message);
        Assertions.assertEquals(message, exception.getMessage());
    }
}
