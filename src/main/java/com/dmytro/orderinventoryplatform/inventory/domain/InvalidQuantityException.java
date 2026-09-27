package com.dmytro.orderinventoryplatform.inventory.domain;

import com.dmytro.orderinventoryplatform.shared.domain.InvalidInputException;

public class InvalidQuantityException extends InvalidInputException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}
