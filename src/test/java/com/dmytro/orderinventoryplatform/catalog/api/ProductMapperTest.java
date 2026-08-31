package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class ProductMapperTest {

    @Test
    public void toResponse_mapsAllFields() {
        ProductMapper productMapper = new ProductMapper();
        Category category = new Category("Test Category", "description", null);
        Product product = new Product("Test Product", "This is a test product", BigDecimal.valueOf(9.99), category, true);
        ProductResponse response = productMapper.toResponse(product);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(product.getId(), response.id());
        Assertions.assertEquals(product.getName(), response.name());
        Assertions.assertEquals(product.getDescription(), response.description());
        Assertions.assertEquals(product.getPrice(), response.price());
        Assertions.assertEquals(product.getCategory().getId(), response.categoryId());
        Assertions.assertEquals(product.isActive(), response.active());
        Assertions.assertEquals(product.getCreatedAt(), response.createdAt());
        Assertions.assertEquals(product.getUpdatedAt(), response.updatedAt());
    }
}