package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CategoryTest {
    @Test
    void shouldCreateCategory() {
        Category category = new Category("Books", "Book category", null);
        Assertions.assertEquals("Books", category.getName());
        Assertions.assertEquals("Book category", category.getDescription());
        Assertions.assertNull(category.getParentCategory());
    }

    @Test
    void shouldCreateSubCategory() {
        Category category1 = new Category("Books", "Book category", null);
        Category category2 = new Category("Fantasy", "Book category Fantasy", category1);
        Assertions.assertEquals("Fantasy", category2.getName());
        Assertions.assertEquals("Book category Fantasy", category2.getDescription());
        Assertions.assertEquals(category1, category2.getParentCategory());
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Category(null, "Book category", null);
        });
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Category("   ", "Book category", null);
        });
    }

    @Test
    void shouldThrowWhenSetNameToBlankOrNull() {
        Category category = new Category("Books", "Book category", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            category.setName(null);
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            category.setName("   ");
        });
    }
}
