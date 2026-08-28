package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class ProductTest {
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category("Books", "Book category", null);
        product = new Product("Product Name", "Product Description", new BigDecimal("29.99"), category, true);
    }

    @Test
    void shouldCreateProduct() {
        Assertions.assertNotNull(product);
        Assertions.assertEquals("Product Name", product.getName());
        Assertions.assertEquals("Product Description", product.getDescription());
        Assertions.assertEquals(new BigDecimal("29.99"), product.getPrice());
        Assertions.assertEquals(category, product.getCategory());
        Assertions.assertTrue(product.isActive());
    }

    @Test
    void shouldCreateProductWithoutDescription() {
        Product product = new Product("Product Name", null, new BigDecimal("29.99"), category, true);
        Assertions.assertNotNull(product);
        Assertions.assertEquals("Product Name", product.getName());
        Assertions.assertNull(product.getDescription());
        Assertions.assertEquals(new BigDecimal("29.99"), product.getPrice());
        Assertions.assertEquals(category, product.getCategory());
        Assertions.assertTrue(product.isActive());
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product(null, "Product Description", new BigDecimal("29.99"), category, true));
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("   ", "Product Description", new BigDecimal("29.99"), category, true));
    }

    @Test
    void shouldThrowWhenPriceIsNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", new BigDecimal("-1.00"), category, true));
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", null, category, true));
    }

    @Test
    void shouldThrowWhenCategoryIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", new BigDecimal("29.99"), null, true));
    }

    @Test
    void shouldThrowWhenActiveIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", new BigDecimal("29.99"), category, null));
    }

    @Test
    void shouldUpdateFields() {
        product.setName("Updated Name");
        product.setDescription("Updated Description");
        product.setPrice(new BigDecimal("39.99"));
        product.setActive(false);
        Assertions.assertEquals("Updated Name", product.getName());
        Assertions.assertEquals("Updated Description", product.getDescription());
        Assertions.assertEquals(new BigDecimal("39.99"), product.getPrice());
        Assertions.assertFalse(product.isActive());
    }

    @Test
    void shouldThrowWhenUpdatingNameToNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> product.setName(null));
    }

    @Test
    void shouldThrowWhenUpdatingNameToBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> product.setName("   "));
    }

    @Test
    void shouldThrowWhenUpdatingPriceToNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> product.setPrice(new BigDecimal("-1.00")));
    }

    @Test
    void shouldThrowWhenUpdatingPriceToNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> product.setPrice(null));
    }

    @Test
    void shouldThrowWhenUpdatingCategoryToNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> product.setCategory(null));
    }

    @Test
    void shouldThrowWhenUpdatingActiveToNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> product.setActive(null));
    }
}
