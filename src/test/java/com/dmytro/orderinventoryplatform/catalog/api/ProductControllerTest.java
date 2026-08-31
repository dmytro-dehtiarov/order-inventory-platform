package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.application.ProductService;
import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import com.dmytro.orderinventoryplatform.catalog.domain.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    private Category category;

    @BeforeEach
    public void setUp() {
        category = new Category("Test Category", "description", null);
    }

    @Test
    public void createProduct_returns201_whenRequestIsValid() throws Exception {
        Product product = new Product("Test Product", "This is a test product", BigDecimal.valueOf(9.99), category, true);
        ProductResponse response = new ProductResponse(1L, "Test Product", "This is a test product", BigDecimal.valueOf(9.99), 1L, true, null, null);

        when(productService.createProduct("Test Product", "This is a test product", BigDecimal.valueOf(9.99), 1L, true)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductRequest request = new ProductRequest("Test Product", "This is a test product", BigDecimal.valueOf(9.99), 1L, true);

        mockMvc.perform(post("/api/v1/catalog/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    public void createProduct_returns400_whenRequestIsInvalid() throws Exception {
        ProductRequest request = new ProductRequest("", "This is a test product", BigDecimal.valueOf(9.99), 1L, true);

        mockMvc.perform(post("/api/v1/catalog/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getProduct_returns200_whenProductExists() throws Exception {
        Product product = new Product("Test Product", "This is a test product", BigDecimal.valueOf(9.99), category, true);
        ProductResponse response = new ProductResponse(1L, "Test Product", "This is a test product", BigDecimal.valueOf(9.99), 1L, true, null, null);

        when(productService.getProductById(1L)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        mockMvc.perform(get("/api/v1/catalog/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    public void getProduct_returns404_whenProductDoesNotExist() throws Exception {
        when(productService.getProductById(1L)).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/catalog/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getProduct_returns400_whenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getProducts_returns200_withPageOfProducts() throws Exception {
        Product product1 = new Product("Product 1", "Description 1", BigDecimal.valueOf(9.99), category, true);
        Product product2 = new Product("Product 2", "Description 2", BigDecimal.valueOf(19.99), category, true);

        PageImpl<Product> page = new PageImpl<>(List.of(product1, product2), PageRequest.of(0, 10), 2);

        when(productService.listProducts(any(Pageable.class))).thenReturn(page);
        when(productMapper.toResponse(product1)).thenReturn(new ProductResponse(1L, "Product 1", "Description 1", BigDecimal.valueOf(9.99), 1L, true, null, null));
        when(productMapper.toResponse(product2)).thenReturn(new ProductResponse(2L, "Product 2", "Description 2", BigDecimal.valueOf(19.99), 1L, true, null, null));

        mockMvc.perform(get("/api/v1/catalog/products?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                .andExpect(jsonPath("$.content[1].name").value("Product 2"));
    }

    @Test
    public void updateProduct_returns200_whenRequestIsValid() throws Exception {
        Product newProduct = new Product("Product 1", "Description 1", BigDecimal.valueOf(9.99), category, true);
        ProductResponse response = new ProductResponse(1L, "Product 1", "Description 1", BigDecimal.valueOf(9.99), 1L, true, null, null);

        when(productService.updateProduct(1L, "Product 1", "Description 1", BigDecimal.valueOf(9.99), 1L, true)).thenReturn(newProduct);
        when(productMapper.toResponse(newProduct)).thenReturn(response);

        ProductRequest request = new ProductRequest("Product 1", "Description 1", BigDecimal.valueOf(9.99), 1L, true);

        mockMvc.perform(put("/api/v1/catalog/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    public void updateProduct_returns404_whenProductDoesNotExist() throws Exception {
        when(productService.updateProduct(1L, "Product 1", "Description 1", BigDecimal.valueOf(9.99), 1L, true)).thenThrow(new ProductNotFoundException("Product not found"));

        ProductRequest request = new ProductRequest("Product 1", "Description 1", BigDecimal.valueOf(9.99), 1L, true);

        mockMvc.perform(put("/api/v1/catalog/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateProduct_returns400_whenRequestIsInvalid() throws Exception {
        ProductRequest request = new ProductRequest("", "Description 1", BigDecimal.valueOf(9.99), 1L, true);

        mockMvc.perform(put("/api/v1/catalog/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateProduct_returns400_whenIdIsInvalid() throws Exception {
        ProductRequest request = new ProductRequest("Product 1", "Description 1", BigDecimal.valueOf(9.99), 1L, true);

        mockMvc.perform(put("/api/v1/catalog/products/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deactivateProduct_returns204_whenProductExists() throws Exception {
        mockMvc.perform(patch("/api/v1/catalog/products/1/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deactivateProduct_returns404_whenProductDoesNotExist() throws Exception {
        doThrow(new ProductNotFoundException("Product not found")).when(productService).deactivateProduct(1L);

        mockMvc.perform(patch("/api/v1/catalog/products/1/deactivate"))
                .andExpect(status().isNotFound());
    }
}