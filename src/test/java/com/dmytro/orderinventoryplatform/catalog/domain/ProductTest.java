package com.dmytro.orderinventoryplatform.catalog.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class ProductTest {
    @Test
    void shouldCreateProduct() {
        Category category = new Category("Books", "Book category", null);
        Product product = new Product("Product Name", "Product Description", new BigDecimal("29.99"), category, true);
        Assertions.assertNotNull(product);
        Assertions.assertEquals("Product Name", product.getName());
        Assertions.assertEquals("Product Description", product.getDescription());
        Assertions.assertEquals(new BigDecimal("29.99"), product.getPrice());
        Assertions.assertEquals(category, product.getCategory());
        Assertions.assertTrue(product.isActive());
    }

    @Test
    void shouldCreateProductWithoutDescription() {
        Category category = new Category("Books", "Book category", null);
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
        Category category = new Category("Books", "Book category", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product(null, "Product Description", new BigDecimal("29.99"), category, true));
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        Category category = new Category("Books", "Book category", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("   ", "Product Description", new BigDecimal("29.99"), category, true));
    }

    @Test
    void shouldThrowWhenPriceIsNegative() {
        Category category = new Category("Books", "Book category", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", new BigDecimal("-1.00"), category, true));
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        Category category = new Category("Books", "Book category", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", null, category, true));
    }

    @Test
    void shouldThrowWhenCategoryIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", new BigDecimal("29.99"), null, true));
    }

    @Test
    void shouldThrowWhenActiveIsNull() {
        Category category = new Category("Books", "Book category", null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Product("Product Name", "Product Description", new BigDecimal("29.99"), category, null));
    }
}
