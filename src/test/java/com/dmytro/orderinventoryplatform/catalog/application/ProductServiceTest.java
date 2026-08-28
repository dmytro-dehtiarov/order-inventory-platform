package com.dmytro.orderinventoryplatform.catalog.application;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.catalog.domain.CategoryNotFoundException;
import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import com.dmytro.orderinventoryplatform.catalog.domain.ProductNotFoundException;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.CategoryRepository;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
public class ProductServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    private Category category;

    @BeforeEach
    public void setUp() {
        category = new Category("Books", "desc", null);
    }

    @Test
    public void createProduct_savesProduct_whenCategoryExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct("Test Product", "Test Description", new BigDecimal("19.99"), 1L, true);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("Test Product", result.getName());
        Assertions.assertSame(category, result.getCategory());
    }

    @Test
    public void createProduct_throwsCategoryNotFoundException_whenParentDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(CategoryNotFoundException.class, () -> {
            productService.createProduct("Test Product", "Test Description", new BigDecimal("19.99"), 1L, true);
        });
    }

    @Test
    public void updateProduct_updatesFields_whenValid() {
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product product = new Product("Old Name", "Old Description", new BigDecimal("9.99"), category, true);
        Category newCategory = new Category("New Category", "New Desc", null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        Product updatedProduct = productService.updateProduct(1L, "New Name", "New Description", new BigDecimal("19.99"), 2L, false);

        Assertions.assertNotNull(updatedProduct);
        Assertions.assertEquals("New Name", updatedProduct.getName());
        Assertions.assertEquals("New Description", updatedProduct.getDescription());
        Assertions.assertEquals(new BigDecimal("19.99"), updatedProduct.getPrice());
        Assertions.assertEquals(newCategory, updatedProduct.getCategory());
        Assertions.assertFalse(updatedProduct.isActive());
    }

    @Test
    public void updateProduct_throwsProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(ProductNotFoundException.class, () -> {
            productService.updateProduct(1L, "New Name", "New Description", new BigDecimal("19.99"), 2L, false);
        });
    }

    @Test
    public void updateProduct_throwsCategoryNotFoundException_whenCategoryDoesNotExist() {
        Product product = new Product("Old Name", "Old Description", new BigDecimal("9.99"), category, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(CategoryNotFoundException.class, () -> {
            productService.updateProduct(1L, "New Name", "New Description", new BigDecimal("19.99"), 2L, false);
        });
    }

    @Test
    public void getProductById_returnsProduct_whenExists() {
        Product product = new Product("Test Product", "Test Description", new BigDecimal("19.99"), category, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(product, result);
    }

    @Test
    public void getProductById_throwsProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(ProductNotFoundException.class, () -> {
            productService.getProductById(1L);
        });
    }

    @Test
    public void listProducts_returnsPageFromRepository() {
        Page<Product> mockPage = new PageImpl<>(List.of(
                new Product("Product 1", "Description 1", new BigDecimal("9.99"), category, true),
                new Product("Product 2", "Description 2", new BigDecimal("19.99"), category, true)
        ));

        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByActiveTrue(pageable)).thenReturn(mockPage);

        Page<Product> result = productService.listProducts(pageable);

        Assertions.assertEquals(mockPage, result);
    }

    @Test
    public void deactivateProduct_deactivatesProduct_whenExists() {
        Product product = new Product("Test Product", "Test Description", new BigDecimal("19.99"), category, true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deactivateProduct(1L);

        Assertions.assertFalse(product.isActive());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void deactivateProduct_throwsProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(ProductNotFoundException.class, () -> {
            productService.deactivateProduct(1L);
        });
    }

    @Test
    public void deactivateProduct_isIdempotent_whenProductAlreadyInactive() {
        Product product = new Product("Test Product", "Test Description", new BigDecimal("19.99"), category, false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deactivateProduct(1L);

        Assertions.assertFalse(product.isActive());
        verify(productRepository, times(0)).save(product);
    }

    @Test
    public void getSummaries_returnsSummariesForFoundProducts() {
        Product product1 = new Product("Product 1", "Description 1", new BigDecimal("9.99"), category, true);
        Product product2 = new Product("Product 2", "Description 2", new BigDecimal("19.99"), category, true);

        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(product1, product2));

        List<ProductSummary> summaries = productService.getSummaries(List.of(1L, 2L));

        Assertions.assertNotNull(summaries);
        Assertions.assertEquals(2, summaries.size());
        Assertions.assertTrue(summaries.stream().anyMatch(s -> s.name().equals("Product 1")));
        Assertions.assertTrue(summaries.stream().anyMatch(s -> s.name().equals("Product 2")));
    }

    @Test
    public void getSummaries_throwsProductNotFoundException_whenProductDoesNotExist() {
        Product product1 = new Product("Product 1", "Description 1", new BigDecimal("9.99"), category, true);

        when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(product1));

        Assertions.assertThrowsExactly(ProductNotFoundException.class, () -> {
            productService.getSummaries(List.of(1L, 2L));
        });
    }
}
