package com.dmytro.orderinventoryplatform.shared.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConflictExceptionTest {
    @Test
    public void testConflictException() {
        String message = "Conflict occurred";
        ConflictException exception = new ConflictException(message) {
        };

        Assertions.assertEquals(message, exception.getMessage());
    }
}
