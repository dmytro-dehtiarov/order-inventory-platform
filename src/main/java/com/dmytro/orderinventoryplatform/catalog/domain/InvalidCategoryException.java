package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.InvalidInputException;

public class InvalidCategoryException extends InvalidInputException {
    public InvalidCategoryException(String message) {
        super(message);
    }
}
