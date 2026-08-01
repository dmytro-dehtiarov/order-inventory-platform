package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;

public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
