package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.InvalidInputException;

public class InvalidProductException extends InvalidInputException {
    public InvalidProductException(String message) {
        super(message);
    }
}
