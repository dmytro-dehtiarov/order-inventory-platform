package com.dmytro.orderinventoryplatform.catalog.application;

import com.dmytro.orderinventoryplatform.catalog.domain.*;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.CategoryRepository;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private CategoryService categoryService;

    @Test
    public void createCategory_savesTopLevelCategory_whenParentIsNull() {
        when(categoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.createCategory("Books", "desc", null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Books", result.getName());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    public void createCategory_savesCategory_whenParentIsNotNull() {
        when(categoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Category parent = new Category("Parent", "desc", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));

        Category result = categoryService.createCategory("Books", "desc", 1L);

        Assertions.assertSame(parent, result.getParentCategory());
    }

    @Test
    public void createCategory_throwsCategoryNotFoundException_whenParentDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(CategoryNotFoundException.class, () -> {
            categoryService.createCategory("Books", "desc", 1L);
        });
    }

    @Test
    public void getCategory_returnsCategory_whenFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category("Books", "desc", null)));

        Category result = categoryService.getCategory(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Books", result.getName());
    }

    @Test
    public void getCategory_throwsCategoryNotFoundException_whenNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(CategoryNotFoundException.class, () -> {
            categoryService.getCategory(1L);
        });
    }

    @Test
    public void listCategories_returnsPageFromRepository() {
        Page<Category> mockPage = new PageImpl<>(List.of(new Category("Books", "desc", null), new Category("Toys", "desc", null)));
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.findAll(pageable)).thenReturn(mockPage);

        Assertions.assertEquals(mockPage, categoryService.listCategories(pageable));
    }

    @Test
    public void updateCategory_updatesFields_whenParentIsNull() {
        Category oldCategory = new Category("Old Name", "Old desc", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(oldCategory));
        when(categoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Category newCategory = categoryService.updateCategory(1L, "New Name", "New desc", null);

        Assertions.assertEquals("New Name", newCategory.getName());
        Assertions.assertEquals("New desc", newCategory.getDescription());
    }

    @Test
    public void updateCategory_throwsCategoryCycleException_whenParentIsSelf() {
        Category oldCategory = new Category("Old Name", "Old desc", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(oldCategory));

        Assertions.assertThrowsExactly(CategoryCycleException.class, () -> {
            categoryService.updateCategory(1L, "New Name", "New desc", 1L);
        });
    }

    @Test
    public void updateCategory_throwsCategoryNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(CategoryNotFoundException.class, () -> {
            categoryService.updateCategory(1L, "New Name", "New desc", null);
        });
    }

    @Test
    public void updateCategory_throwsCategoryNotFoundException_whenParentDoesNotExist() {
        Category oldCategory = new Category("Old Name", "Old desc", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(oldCategory));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(CategoryNotFoundException.class, () -> {
            categoryService.updateCategory(1L, "New Name", "New desc", 2L);
        });
    }

    @Test
    public void deleteCategory_deletesCategory_whenNoGuardsTrip() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        Assertions.assertDoesNotThrow(() -> {
            categoryService.deleteCategory(1L);
        });
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    public void deleteCategory_throwsCategoryNotFoundException_whenCategoryNotFound() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        Assertions.assertThrowsExactly(CategoryNotFoundException.class, () -> {
            categoryService.deleteCategory(1L);
        });
    }

    @Test
    public void deleteCategory_throwsCategoryInUseException_whenHasChildCategories() {
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(categoryRepository.existsByParentCategoryId(2L)).thenReturn(true);

        Assertions.assertThrowsExactly(CategoryInUseException.class, () -> {
            categoryService.deleteCategory(2L);
        });
    }

    @Test
    public void deleteCategory_throwsCategoryInUseException_whenHasProducts() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        Assertions.assertThrowsExactly(CategoryInUseException.class, () -> {
            categoryService.deleteCategory(1L);
        });
    }
}