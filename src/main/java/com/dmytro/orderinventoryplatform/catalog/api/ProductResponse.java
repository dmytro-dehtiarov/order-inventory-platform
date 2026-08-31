package com.dmytro.orderinventoryplatform.catalog.api;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Long categoryId,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
