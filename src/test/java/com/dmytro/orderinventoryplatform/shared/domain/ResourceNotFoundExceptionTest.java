package com.dmytro.orderinventoryplatform.shared.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class ResourceNotFoundExceptionTest {
    @Test
    public void testResourceNotFoundException() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message) {
        };
        Assertions.assertEquals(message, exception.getMessage());
    }
}
