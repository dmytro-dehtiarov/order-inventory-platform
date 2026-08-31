package com.dmytro.orderinventoryplatform.catalog.api;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response body returned by {@link ProductController} for a single
 * product. {@code categoryId} exposes only the category's id, never the
 * nested {@code Category} object, to keep the API response flat.
 *
 * @param id the product id
 * @param name the product name
 * @param description the product description
 * @param price the unit price
 * @param categoryId the id of the category this product belongs to
 * @param active whether the product is active/sellable
 * @param createdAt when the product was created
 * @param updatedAt when the product was last updated
 */
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
