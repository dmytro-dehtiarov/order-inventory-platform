package com.dmytro.orderinventoryplatform.catalog.api;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentCategoryId,
        Instant createdAt,
        Instant updatedAt
) {}
