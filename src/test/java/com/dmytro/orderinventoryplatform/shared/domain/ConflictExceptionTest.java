package com.dmytro.orderinventoryplatform.shared.domain;

import org.junit.jupiter.api.Test;

public class ConflictExceptionTest {
    @Test
    public void testConflictException() {
        String message = "Conflict occurred";
        ConflictException exception = new ConflictException(message) {
        };

        assert exception.getMessage().equals(message);
    }
}
