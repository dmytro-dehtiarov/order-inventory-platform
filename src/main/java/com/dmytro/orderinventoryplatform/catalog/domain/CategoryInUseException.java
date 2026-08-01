package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;

public class CategoryInUseException extends ConflictException {
    public CategoryInUseException(String message) {
        super(message);
    }
}
