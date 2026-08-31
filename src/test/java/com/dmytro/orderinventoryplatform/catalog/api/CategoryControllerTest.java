package com.dmytro.orderinventoryplatform.catalog.api;

import com.dmytro.orderinventoryplatform.catalog.application.CategoryService;
import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.catalog.domain.CategoryCycleException;
import com.dmytro.orderinventoryplatform.catalog.domain.CategoryInUseException;
import com.dmytro.orderinventoryplatform.catalog.domain.CategoryNotFoundException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    @Test
    public void createCategory_returns201_whenRequestIsValid() throws Exception {
        Category category = new Category("Books", "desk", null);
        CategoryResponse response = new CategoryResponse(1L, "Books", "desk", null, null, null);

        when(categoryService.createCategory("Books", "desk", null)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryRequest request = new CategoryRequest("Books", "desk", null);

        mockMvc.perform(post("/api/v1/catalog/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    public void createCategory_returns400_whenRequestIsInvalid() throws Exception {
        CategoryRequest request = new CategoryRequest("", "desk", null);

        mockMvc.perform(post("/api/v1/catalog/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getCategory_returns200_whenCategoryExists() throws Exception {
        Category category = new Category("Books", "desk", null);
        CategoryResponse response = new CategoryResponse(1L, "Books", "desk", null, null, null);

        when(categoryService.getCategory(1L)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        mockMvc.perform(get("/api/v1/catalog/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    public void getCategory_returns404_whenCategoryDoesNotExist() throws Exception {
        when(categoryService.getCategory(anyLong())).thenThrow(new CategoryNotFoundException("Category not found"));

        mockMvc.perform(get("/api/v1/catalog/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getCategory_returns400_whenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getCategories_returns200_withPageOfCagegories() throws Exception {
        Category category = new Category("Books", "desk", null);
        Category category2 = new Category("Books1", "desk2", null);

        PageImpl<Category> page = new PageImpl<>(List.of(category, category2), PageRequest.of(0, 10), 2);

        when(categoryService.listCategories(any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toResponse(category)).thenReturn(new CategoryResponse(1L, "Books", "desk", null, null, null));
        when(categoryMapper.toResponse(category2)).thenReturn(new CategoryResponse(2L, "Books1", "desk2", null, null, null));

        mockMvc.perform(get("/api/v1/catalog/categories?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Books"))
                .andExpect(jsonPath("$.content[1].name").value("Books1"));
    }

    @Test
    public void updateCategory_returns200_whenRequestIsValid() throws Exception {
        Category newCategory = new Category("NewName", "desk", null);
        CategoryResponse response = new CategoryResponse(1L, "NewName", "desk", null, null, null);

        when(categoryService.updateCategory(1L, "NewName", "desk", null)).thenReturn(newCategory);
        when(categoryMapper.toResponse(newCategory)).thenReturn(response);

        CategoryRequest request = new CategoryRequest("NewName", "desk", null);

        mockMvc.perform(put("/api/v1/catalog/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    public void updateCategory_returns404_whenCategoryDoesNotExist() throws Exception {
        when(categoryService.updateCategory(anyLong(), anyString(), anyString(), any()))
                .thenThrow(new CategoryNotFoundException("Category not found"));

        CategoryRequest request = new CategoryRequest("NewName", "desk", null);

        mockMvc.perform(put("/api/v1/catalog/categories/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateCategory_returns409_whenCycleDetected() throws Exception {
        when(categoryService.updateCategory(1L, "NewName", "desk", 1L))
                .thenThrow(new CategoryCycleException("Cycle detected in category hierarchy"));

        CategoryRequest request = new CategoryRequest("NewName", "desk", 1L);

        mockMvc.perform(put("/api/v1/catalog/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateCategory_returns400_whenIdIsInvalid() throws Exception {
        CategoryRequest request = new CategoryRequest("NewName", "desk", null);

        mockMvc.perform(put("/api/v1/catalog/categories/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateCategory_returns400_whenRequestIsInvalid() throws Exception {
        CategoryRequest request = new CategoryRequest("", "desk", null);

        mockMvc.perform(put("/api/v1/catalog/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteCategory_returns204_whenCategoryExists() throws Exception {
        mockMvc.perform(delete("/api/v1/catalog/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteCategory_returns404_whenCategoryDoesNotExist() throws Exception {
        doThrow(new CategoryNotFoundException("Category not found")).when(categoryService).deleteCategory(999L);

        mockMvc.perform(delete("/api/v1/catalog/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteCategory_returns409_whenCategoryInUse() throws Exception {
        doThrow(new CategoryInUseException("Category is in use")).when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/catalog/categories/1"))
                .andExpect(status().isConflict());
    }
}
