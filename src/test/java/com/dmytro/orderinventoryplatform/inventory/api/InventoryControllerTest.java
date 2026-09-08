package com.dmytro.orderinventoryplatform.inventory.api;

import com.dmytro.orderinventoryplatform.inventory.application.InventoryService;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItemNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private InventoryMapper inventoryMapper;

    @Test
    public void createInventoryItem_returns201_whenRequestIsValid() throws Exception {
        InventoryItem inventoryItem = new InventoryItem(1L, 10);
        InventoryResponse response = new InventoryResponse(1L, 10, 0, 10);

        when(inventoryService.createInventoryItem(1L, 10)).thenReturn(inventoryItem);
        when(inventoryMapper.toResponse(inventoryItem)).thenReturn(response);

        InventoryItemCreateRequest request = new InventoryItemCreateRequest(1L, 10);

        mockMvc.perform(post("/api/v1/inventory/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(header().string("Location", endsWith("/api/v1/inventory/items/1")));
    }

    @Test
    public void createInventoryItem_returns400_whenRequestIsInvalid() throws Exception {
        InventoryItemCreateRequest request = new InventoryItemCreateRequest(null, 10);

        mockMvc.perform(post("/api/v1/inventory/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getInventoryItem_returns200_whenItemExists() throws Exception {
        InventoryItem inventoryItem = new InventoryItem(1L, 10);
        InventoryResponse response = new InventoryResponse(1L, 10, 0, 10);

        when(inventoryService.getInventoryItem(1L)).thenReturn(inventoryItem);
        when(inventoryMapper.toResponse(inventoryItem)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventory/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void getInventoryItem_returns404_whenItemDoesNotExist() throws Exception {
        when(inventoryService.getInventoryItem(1L)).thenThrow(new InventoryItemNotFoundException(1L));

        mockMvc.perform(get("/api/v1/inventory/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getInventoryItem_returns400_whenIdIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/items/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void adjustInventoryItem_returns200_whenRequestIsValid() throws Exception {
        InventoryItem inventoryItem = new InventoryItem(1L, 5);
        InventoryResponse response = new InventoryResponse(1L, 5, 0, 5);

        when(inventoryService.adjustInventoryItem(1L, 5)).thenReturn(inventoryItem);
        when(inventoryMapper.toResponse(inventoryItem)).thenReturn(response);

        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(5);

        mockMvc.perform(patch("/api/v1/inventory/items/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.availableQuantity").value(5));
    }

    @Test
    public void adjustInventoryItem_returns400_whenQuantityNegative() throws Exception {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(-5);

        mockMvc.perform(patch("/api/v1/inventory/items/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void adjustInventoryItem_returns404_whenItemDoesNotExist() throws Exception {
        when(inventoryService.adjustInventoryItem(1L, 5)).thenThrow(new InventoryItemNotFoundException(1L));

        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest(5);

        mockMvc.perform(patch("/api/v1/inventory/items/1/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}