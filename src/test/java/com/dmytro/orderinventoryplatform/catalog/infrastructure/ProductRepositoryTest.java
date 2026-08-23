package com.dmytro.orderinventoryplatform.catalog.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import com.dmytro.orderinventoryplatform.shared.testsupport.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

class ProductRepositoryTest extends AbstractIntegrationTest {
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EntityManager entityManager;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = categoryRepository.save(new Category("Books", "Book category", null));
    }

    private Product createAndSaveProduct(String name, BigDecimal price, boolean active) {
        Product product = new Product(name, "description", price, testCategory, active);
        return productRepository.save(product);
    }

    @Test
    void shouldCreateProduct() {
        Product savedProduct = createAndSaveProduct("Test Product", new BigDecimal("19.99"), true);

        Assertions.assertNotNull(savedProduct.getId());
        Assertions.assertNotNull(savedProduct.getName());
        Assertions.assertNotNull(savedProduct.getPrice());
        Assertions.assertTrue(savedProduct.isActive());
    }

    @Test
    void existsByCategoryId_returnsTrue_whenProductExists() {
        createAndSaveProduct("Test Product", new BigDecimal("19.99"), true);

        Assertions.assertTrue(productRepository.existsByCategoryId(testCategory.getId()));
    }

    @Test
    void existsByCategoryId_returnsFalse_whenProductDoesNotExists() {
        Assertions.assertFalse(productRepository.existsByCategoryId(testCategory.getId()));
    }

    @Test
    void findByActiveTrue_returnsOnlyActiveProducts() {
        createAndSaveProduct("Test Product", new BigDecimal("19.99"), true);
        createAndSaveProduct("Test Product2", new BigDecimal("19.99"), false);

        Page<Product> activeProducts = productRepository.findByActiveTrue(PageRequest.of(0, 10));

        Assertions.assertEquals(1, activeProducts.getContent().size());
        Assertions.assertTrue(activeProducts.getContent().get(0).isActive());
    }

    @Test
    void findByActiveTrue_respectsPageSize() {
        createAndSaveProduct("Test Product", new BigDecimal("19.99"), true);
        createAndSaveProduct("Test Product2", new BigDecimal("19.99"), true);
        createAndSaveProduct("Test Product3", new BigDecimal("19.99"), true);

        Page<Product> firstPage = productRepository.findByActiveTrue(PageRequest.of(0, 2));

        Assertions.assertEquals(2, firstPage.getContent().size());
        Assertions.assertEquals(3, firstPage.getTotalElements());
    }

    @Test
    void productPrice_survivesRoundTripThroughDb() {
        Product savedProduct = createAndSaveProduct("Test Product", new BigDecimal("19.9"), true);

        entityManager.clear();

        Product foundProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        Assertions.assertEquals(0, new BigDecimal("19.9").compareTo(foundProduct.getPrice()));
    }
}