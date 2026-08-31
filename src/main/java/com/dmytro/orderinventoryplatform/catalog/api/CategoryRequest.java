package com.dmytro.orderinventoryplatform.catalog.api;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,
        String description,
        Long parentCategoryId
) {}
