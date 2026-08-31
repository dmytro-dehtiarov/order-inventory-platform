package com.dmytro.orderinventoryplatform.catalog.api;

import java.time.Instant;

/**
 * Response body returned by {@link CategoryController} for a single
 * category. {@code parentCategoryId} exposes only the parent's id, never
 * the nested {@code Category} object, to keep the API response flat.
 *
 * @param id the category id
 * @param name the category name
 * @param description the category description
 * @param parentCategoryId the id of the parent category, or {@code null}
 *                          if this is a top-level category
 * @param createdAt when the category was created
 * @param updatedAt when the category was last updated
 */
public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentCategoryId,
        Instant createdAt,
        Instant updatedAt
) {}
