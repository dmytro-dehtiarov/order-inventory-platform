package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryInUseExceptionTest {
    @Test
    public void testCategoryInUseExceptionTest() {
        String message = "category in use";
        CategoryInUseException exception = new CategoryInUseException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }
}
