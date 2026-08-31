package com.dmytro.orderinventoryplatform.catalog.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body for creating or updating a product via
 * {@link ProductController}. Carries no {@code id} - on create it doesn't
 * exist yet, and on update it comes from the path, never the body.
 *
 * @param name a non-blank product name
 * @param description an optional, free-text description
 * @param price the unit price; must not be negative
 * @param categoryId the id of the category this product belongs to
 * @param active whether the product should be active/sellable
 */
public record ProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal price,
        @NotNull Long categoryId,
        @NotNull Boolean active
) {
}
