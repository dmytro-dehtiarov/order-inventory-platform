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

@RestController
@RequestMapping("/api/v1/catalog/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        CategoryResponse categoryResponse = categoryMapper.toResponse(category);

        return ResponseEntity.ok(categoryResponse);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getCategories(Pageable pageable) {
        Page<Category> categories = categoryService.listCategories(pageable);
        Page<CategoryResponse> categoryResponses = categories.map(categoryMapper::toResponse);

        return ResponseEntity.ok(categoryResponses);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
