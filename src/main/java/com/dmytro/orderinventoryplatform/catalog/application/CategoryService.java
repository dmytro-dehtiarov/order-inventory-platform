package com.dmytro.orderinventoryplatform.catalog.application;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;

import com.dmytro.orderinventoryplatform.catalog.domain.CategoryCycleException;
import com.dmytro.orderinventoryplatform.catalog.domain.CategoryInUseException;
import com.dmytro.orderinventoryplatform.catalog.domain.CategoryNotFoundException;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.CategoryRepository;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use-case service for {@link Category}: owns the transactional boundaries
 * and business validation that a repository alone can't express (parent
 * existence, acyclic hierarchy, delete guards against orphaning children or
 * products).
 *
 * <p>No interface — this is the single implementation, and an interface
 * here would be pure ceremony (architecture.md reserves interfaces for
 * cases with more than one implementation).
 */
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Walks the ancestor chain starting at {@code parentCategoryId}, one id
     * at a time, checking whether it ever reaches {@code categoryId}.
     *
     * <p>Deliberately walks ids via {@link CategoryRepository#findParentCategoryIdByCategoryId}
     * rather than the in-memory {@code Category} object graph
     * ({@code getParentCategory()}): the latter depends on lazy-loaded
     * associations being initialized, which id-based repository lookups
     * don't need to assume.
     *
     * @param categoryId the id of the category being updated
     * @param parentCategoryId the id of the proposed new parent
     * @throws CategoryCycleException if {@code categoryId} is found
     *                                 anywhere in the ancestor chain of
     *                                 {@code parentCategoryId} (including
     *                                 {@code parentCategoryId} itself,
     *                                 covering the self-parent case)
     */
    private void checkForCycle(Long categoryId, Long parentCategoryId) {
        Long current = parentCategoryId;
        while (current != null) {
            if (current.equals(categoryId)) {
                throw new CategoryCycleException("Category hierarchy cycle detected");
            }
            current = categoryRepository.findParentCategoryIdByCategoryId(current).orElse(null);
        }
    }

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * @param name a non-blank category name
     * @param description an optional, free-text description
     * @param parentCategoryId the id of the parent category, or {@code null}
     *                          for a top-level category
     * @return the saved category, with its generated {@code id} populated
     * @throws CategoryNotFoundException if {@code parentCategoryId} is
     *                                    non-null but no such category exists
     */
    @Transactional
    public Category createCategory(String name, String description, Long parentCategoryId) {
        if (parentCategoryId == null) {
            Category category = new Category(name, description, null);
            return categoryRepository.save(category);
        } else {
            Category parentCategory = categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found"));
            return categoryRepository.save(new Category(name, description, parentCategory));
        }
    }

    /**
     * @param id the category id
     * @return the category
     * @throws CategoryNotFoundException if no category with this id exists
     */
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
    }

    /**
     * @param pageable pagination and sorting parameters
     * @return a page of categories; direct passthrough to the repository,
     *         no filtering
     */
    public Page<Category> listCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    /**
     * Full-replace update: {@code name}, {@code description}, and
     * {@code parentCategoryId} are all applied, not merged field-by-field
     * against the existing category.
     *
     * @param id the id of the category to update
     * @param name a non-blank category name
     * @param description an optional, free-text description
     * @param parentCategoryId the id of the new parent category, or
     *                          {@code null} to make this a top-level category
     * @return the updated category
     * @throws CategoryNotFoundException if {@code id} doesn't exist, or if
     *                                    {@code parentCategoryId} is
     *                                    non-null but doesn't exist
     * @throws CategoryCycleException if {@code parentCategoryId} is this
     *                                 category itself or one of its own
     *                                 descendants, which would make the
     *                                 hierarchy cyclic
     */
    @Transactional
    public Category updateCategory(Long id, String name, String description, Long parentCategoryId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        if (parentCategoryId == null) {
            category.setParentCategory(null);
        } else {
            Category parentCategory = categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found"));
            checkForCycle(id, parentCategoryId);
            category.setParentCategory(parentCategory);
        }

        category.setName(name);
        category.setDescription(description);

        categoryRepository.save(category);
        return category;
    }

    /**
     * Hard delete, guarded against orphaning data: refuses to delete a
     * category that still has child categories or products pointing at it.
     *
     * @param id the id of the category to delete
     * @throws CategoryNotFoundException if no category with this id exists
     * @throws CategoryInUseException if the category has child categories
     *                                 or products still referencing it
     */
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category not found");
        }
        if (categoryRepository.existsByParentCategoryId(id)) {
            throw new CategoryInUseException("Category has sub categories and cannot be deleted");
        }
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("Category has products and cannot be deleted");
        }

        categoryRepository.deleteById(id);
    }
}
