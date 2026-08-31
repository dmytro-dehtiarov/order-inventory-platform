package com.dmytro.orderinventoryplatform.catalog.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal price,
        @NotNull Long categoryId,
        @NotNull Boolean active
) {
}
