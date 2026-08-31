package com.dmytro.orderinventoryplatform.catalog.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating or updating a category via
 * {@link CategoryController}. Carries no {@code id} - on create it doesn't
 * exist yet, and on update it comes from the path, never the body.
 *
 * @param name a non-blank category name
 * @param description an optional, free-text description
 * @param parentCategoryId the id of the parent category, or {@code null}
 *                          for a top-level category
 */
public record CategoryRequest(
        @NotBlank String name,
        String description,
        Long parentCategoryId
) {}
