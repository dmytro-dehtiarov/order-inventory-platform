package com.dmytro.orderinventoryplatform.catalog.domain;

import com.dmytro.orderinventoryplatform.shared.domain.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
