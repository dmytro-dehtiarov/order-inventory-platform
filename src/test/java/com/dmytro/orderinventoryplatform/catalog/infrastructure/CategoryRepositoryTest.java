package com.dmytro.orderinventoryplatform.catalog.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.shared.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CategoryRepositoryTest extends AbstractIntegrationTest {
    @Autowired private CategoryRepository categoryRepository;

    private Category createAndSaveCategory(String name, String description, Category parent) {
        Category category = new Category(name, description, parent);
        return categoryRepository.save(category);
    }

    @Test
    void shouldCreateParentCategory() {
        Category savedCategory = createAndSaveCategory("Books", "Book category", null);
        Assertions.assertNotNull(savedCategory.getId());
    }

    @Test
    void shouldCreateChildCategory() {
        Category savedParentCategory = createAndSaveCategory("Books", "Book category", null);
        Assertions.assertNotNull(savedParentCategory.getId());

        Category savedChildCategory = createAndSaveCategory("Books", "Book category", savedParentCategory);
        Assertions.assertNotNull(savedChildCategory.getId());
    }

    @Test
    void existsByParentCategoryId_returnsTrue_whenChildExists() {
        Category parent = createAndSaveCategory("Books", "Book category", null);
        createAndSaveCategory("Fantasy", "Book category", parent);

        Assertions.assertTrue(categoryRepository.existsByParentCategoryId(parent.getId()));
    }

    @Test
    void existsByParentCategoryId_returnsFalse_whenNoChildExists() {
        Category parent = createAndSaveCategory("Books", "Book category", null);

        Assertions.assertFalse(categoryRepository.existsByParentCategoryId(parent.getId()));
    }
}