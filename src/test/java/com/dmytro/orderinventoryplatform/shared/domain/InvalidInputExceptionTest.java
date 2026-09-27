package com.dmytro.orderinventoryplatform.shared.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvalidInputExceptionTest {
    @Test
    public void testInvalidInputException() {
        String message = "Invalid input provided";
        InvalidInputException exception = new InvalidInputException(message) {
        };

        Assertions.assertEquals(message, exception.getMessage());
    }
}