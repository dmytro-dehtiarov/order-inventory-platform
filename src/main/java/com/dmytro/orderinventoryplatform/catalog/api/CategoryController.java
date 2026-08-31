package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.application.CategoryService;
import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST controller for {@link Category}: exposes CRUD endpoints at
 * {@code /api/v1/catalog/categories}, delegating all business logic and
 * validation to {@link CategoryService} and mapping domain objects to/from
 * DTOs via {@link CategoryMapper}.
 *
 * <p>Error handling (404/409/400) is not done here - it relies entirely on
 * the exceptions thrown by {@code CategoryService} being caught by the
 * application-wide {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1/catalog/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    /**
     * @param categoryRequest the category to create; {@code name} must not
     *                          be blank
     * @return {@code 201 Created}, with a {@code Location} header pointing
     *         at the new category and the created category in the body
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        Category category = categoryService.createCategory(
                categoryRequest.name(),
                categoryRequest.description(),
                categoryRequest.parentCategoryId()
        );

        CategoryResponse categoryResponse = categoryMapper.toResponse(category);

        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}");

        return ResponseEntity.created(builder.buildAndExpand(categoryResponse.id()).toUri()).body(categoryResponse);
    }

    /**
     * @param id the category id
     * @return {@code 200 OK} with the category in the body
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        CategoryResponse categoryResponse = categoryMapper.toResponse(category);

        return ResponseEntity.ok(categoryResponse);
    }

    /**
     * @param pageable pagination and sorting parameters, resolved
     *                  automatically from {@code page}/{@code size}/
     *                  {@code sort} query parameters
     * @return {@code 200 OK} with a page of categories in the body
     */
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getCategories(Pageable pageable) {
        Page<Category> categories = categoryService.listCategories(pageable);
        Page<CategoryResponse> categoryResponses = categories.map(categoryMapper::toResponse);

        return ResponseEntity.ok(categoryResponses);
    }

    /**
     * Full-replace update: every field of {@code categoryRequest} is
     * applied, not merged field-by-field against the existing category.
     *
     * @param id the id of the category to update
     * @param categoryRequest the new field values; {@code name} must not
     *                          be blank
     * @return {@code 200 OK} with the updated category in the body
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest) {
        Category category = categoryService.updateCategory(
                id,
                categoryRequest.name(),
                categoryRequest.description(),
                categoryRequest.parentCategoryId()
        );
        CategoryResponse categoryResponse = categoryMapper.toResponse(category);
        return ResponseEntity.ok(categoryResponse);
    }

    /**
     * Hard delete, guarded against orphaning data by
     * {@link CategoryService#deleteCategory}: fails if the category still
     * has child categories or products.
     *
     * @param id the id of the category to delete
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
