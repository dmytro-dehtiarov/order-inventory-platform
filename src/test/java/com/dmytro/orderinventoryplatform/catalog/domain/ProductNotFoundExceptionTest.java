package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProductNotFoundExceptionTest {
    @Test
    public void testProductNotFoundExceptionTest() {
        String message = "product not found";
        ProductNotFoundException exception = new ProductNotFoundException(message);

        Assertions.assertEquals(message, exception.getMessage());
    }
}
