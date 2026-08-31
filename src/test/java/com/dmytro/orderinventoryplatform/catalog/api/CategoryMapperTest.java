package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CategoryMapperTest {
    private CategoryMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new CategoryMapper();
    }

    @Test
    public void toResponse_mapsAllFields_whenParentIsNull() {
        Category category = new Category("Books", "All kinds of books", null);
        CategoryResponse response = mapper.toResponse(category);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(category.getId(), response.id());
        Assertions.assertEquals(category.getName(), response.name());
        Assertions.assertEquals(category.getDescription(), response.description());
        Assertions.assertNull(response.parentCategoryId());
        Assertions.assertEquals(category.getCreatedAt(), response.createdAt());
        Assertions.assertEquals(category.getUpdatedAt(), response.updatedAt());
    }

    @Test
    public void toResponse_mapsAllFields_whenParentIsNotNull() {
        Category parentCategory = new Category("Books", "All kinds of books", null);
        Category category = new Category("Books", "All kinds of books", parentCategory);

        CategoryResponse response = mapper.toResponse(category);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(category.getId(), response.id());
        Assertions.assertEquals(category.getName(), response.name());
        Assertions.assertEquals(category.getDescription(), response.description());
        Assertions.assertEquals(parentCategory.getId(), response.parentCategoryId());
        Assertions.assertEquals(category.getCreatedAt(), response.createdAt());
        Assertions.assertEquals(category.getUpdatedAt(), response.updatedAt());
    }
}