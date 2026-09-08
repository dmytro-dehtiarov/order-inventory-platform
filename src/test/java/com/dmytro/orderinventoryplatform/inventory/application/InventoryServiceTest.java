package com.dmytro.orderinventoryplatform.inventory.application;

import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItemNotFoundException;
import com.dmytro.orderinventoryplatform.inventory.domain.ReferencedProductNotFoundException;
import com.dmytro.orderinventoryplatform.inventory.infrastructure.InventoryItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {
    @Mock private InventoryItemRepository inventoryItemRepository;
    @InjectMocks private InventoryService inventoryService;

    private final Long productId = 1L;
    private final int quantity = 10;
    private InventoryItem inventoryItem;

    @BeforeEach
    public void setUp() {
        inventoryItem = new InventoryItem(productId, quantity);
    }

    @Test
    public void createInventoryItem_savesInventoryItem_whenProductExists() {
        when(inventoryItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem result = inventoryService.createInventoryItem(productId, quantity);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(productId, result.getId());
        Assertions.assertEquals(quantity, result.getAvailable());
        verify(inventoryItemRepository).save(argThat(item ->
                item.getId().equals(productId) && item.getAvailable() == quantity));
    }

    @Test
    public void createInventoryItem_throwsReferencedProductNotFoundException_whenProductDoesNotExist() {
        when(inventoryItemRepository.save(any())).thenThrow(new DataIntegrityViolationException("Foreign key constraint violation"));

        Assertions.assertThrowsExactly(ReferencedProductNotFoundException.class, () -> {
            inventoryService.createInventoryItem(productId, quantity);
        });
    }

    @Test
    public void getInventoryItem_returnsInventoryItem_whenExists() {
        when(inventoryItemRepository.findById(productId)).thenReturn(Optional.of(inventoryItem));

        InventoryItem result = inventoryService.getInventoryItem(productId);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(productId, result.getId());
        Assertions.assertEquals(quantity, result.getAvailable());
    }

    @Test
    public void getInventoryItem_throwsInventoryItemNotFoundException_whenDoesNotExist() {
        when(inventoryItemRepository.findById(productId)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(InventoryItemNotFoundException.class, () -> {
            inventoryService.getInventoryItem(productId);
        });
    }

    @Test
    public void adjustInventoryItem_adjustsQuantity_whenExists() {
        when(inventoryItemRepository.lockedFindById(productId)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem result = inventoryService.adjustInventoryItem(productId, 5);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(productId, result.getId());
        Assertions.assertEquals(5, result.getAvailable());
    }

    @Test
    public void adjustInventoryItem_throwsInventoryItemNotFoundException_whenDoesNotExist() {
        when(inventoryItemRepository.lockedFindById(productId)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(InventoryItemNotFoundException.class, () -> {
            inventoryService.adjustInventoryItem(productId, 5);
        });
    }
}
