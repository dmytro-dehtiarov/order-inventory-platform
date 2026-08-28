package com.dmytro.orderinventoryplatform.catalog.application;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.catalog.domain.CategoryNotFoundException;
import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import com.dmytro.orderinventoryplatform.catalog.domain.ProductNotFoundException;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.CategoryRepository;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use-case service for {@link Product}: owns the transactional boundaries
 * and business validation that a repository alone can't express (category
 * existence, soft-delete via deactivation instead of physical deletion).
 *
 * <p>No interface — this is the single implementation, and an interface
 * here would be pure ceremony (architecture.md reserves interfaces for
 * cases with more than one implementation).
 */
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * @param name a non-blank product name
     * @param description an optional, free-text description
     * @param price the unit price; must not be negative
     * @param categoryId the id of the category this product belongs to
     * @param active whether the product is active/sellable on creation
     * @return the saved product, with its generated {@code id} populated
     * @throws CategoryNotFoundException if no category with {@code categoryId} exists
     */
    @Transactional
    public Product createProduct(String name, String description, BigDecimal price, Long categoryId, Boolean active) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        return productRepository.save(new Product(name, description, price, category, active));
    }

    /**
     * Full-replace update: every field is applied, not merged field-by-field
     * against the existing product.
     *
     * @param productId the id of the product to update
     * @param name a non-blank product name
     * @param description an optional, free-text description
     * @param price the unit price; must not be negative
     * @param categoryId the id of the new category this product belongs to
     * @param active whether the product should be active/sellable
     * @return the updated product
     * @throws ProductNotFoundException if {@code productId} doesn't exist
     * @throws CategoryNotFoundException if no category with {@code categoryId} exists
     */
    @Transactional
    public Product updateProduct(Long productId, String name, String description, BigDecimal price, Long categoryId, Boolean active) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setActive(active);

        return productRepository.save(product);
    }

    /**
     * @param productId the product id
     * @return the product
     * @throws ProductNotFoundException if no product with this id exists
     */
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    /**
     * @param pageable pagination and sorting parameters
     * @return a page of products, filtered to {@code active=true} only;
     *         deactivated products are excluded by default
     */
    public Page<Product> listProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    /**
     * Soft-delete: sets {@code active=false} instead of physically removing
     * the row, so the product remains readable by id (e.g. from historical
     * orders) while disappearing from the default {@link #listProducts} view.
     *
     * <p>Idempotent: calling this on an already-inactive product is a no-op
     * (no repository write happens), rather than an error.
     *
     * @param productId the id of the product to deactivate
     * @throws ProductNotFoundException if no product with this id exists
     */
    @Transactional
    public void deactivateProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        if (product.isActive()) {
            product.setActive(false);
            productRepository.save(product);
        }
    }

    /**
     * Cross-module read API: returns a read-model DTO per product, never
     * the {@link Product} entity itself, so the {@code orders} module (or
     * any future caller) can't reach into catalog's persistence layer.
     *
     * <p>Bulk lookup by design — takes every id from an order's line items
     * in one call instead of one lookup per line item, avoiding an N+1
     * query pattern in the caller.
     *
     * @param ids the ids of the products to summarize
     * @return one {@link ProductSummary} per id, in no particular order
     * @throws ProductNotFoundException if any id in {@code ids} doesn't
     *                                   correspond to an existing product;
     *                                   the exception message names every
     *                                   missing id
     */
    public List<ProductSummary> getSummaries(List<Long> ids) {
        List<Product> products = productRepository.findAllById(ids);

        if (products.size() != ids.size()) {
            Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            Set<Long> missingIds = ids.stream().filter(productId -> !productIds.contains(productId)).collect(Collectors.toSet());
            throw new ProductNotFoundException("Products not found for ids: " + missingIds);
        }

        List<ProductSummary> summaries = new ArrayList<>();
        for (Product product : products) {
            summaries.add(new ProductSummary(product.getId(), product.getName(), product.getPrice(), product.isActive()));
        }

        return summaries;
    }
}
