package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ConflictException;

public class CategoryCycleException extends ConflictException {
    public CategoryCycleException(String message) {
        super(message);
    }
}
