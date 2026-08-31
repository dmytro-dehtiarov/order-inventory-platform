package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import org.springframework.stereotype.Component;

/**
 * Manual mapper between {@link Category} and {@link CategoryResponse}.
 */
@Component
public class CategoryMapper {
    /**
     * @param category the entity to map
     * @return the equivalent response DTO, with {@code parentCategoryId}
     *         resolved from the parent association if present
     */
    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentCategory() != null ? category.getParentCategory().getId() : null,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
