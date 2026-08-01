package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryNotFoundExceptionTest {
    @Test
    public void testCategoryNotFoundExceptionTest() {
        String message = "category not found";
        CategoryNotFoundException exception = new CategoryNotFoundException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }
}
