package com.dmytro.orderinventoryplatform.inventory.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReferencedProductNotFoundExceptionTest {
    @Test
    public void testReferencedProductNotFoundException() {
        Long productId = 1L;
        ReferencedProductNotFoundException exception = new ReferencedProductNotFoundException(productId);

        Assertions.assertEquals("Referenced product with ID " + productId + " not found.", exception.getMessage());
    }
}
