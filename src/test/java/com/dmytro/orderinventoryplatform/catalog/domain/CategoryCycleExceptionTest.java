package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryCycleExceptionTest {
    @Test
    public void testCategoryCycleExceptionTest() {
        String message = "category cycle detected";
        CategoryCycleException exception = new CategoryCycleException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }
}
