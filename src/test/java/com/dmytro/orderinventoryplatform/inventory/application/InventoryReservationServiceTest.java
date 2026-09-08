package com.dmytro.orderinventoryplatform.inventory.application;

import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItemNotFoundException;
import com.dmytro.orderinventoryplatform.inventory.infrastructure.InventoryItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryReservationServiceTest {
    @Mock private InventoryItemRepository inventoryItemRepository;
    @InjectMocks private InventoryReservationService inventoryReservationService;

    private final Long productId = 1L;
    private final int quantity = 10;
    private InventoryItem inventoryItem;

    @BeforeEach
    public void setUp() {
        inventoryItem = new InventoryItem(productId, quantity);
    }

    @Test
    public void reserveInventoryItem_reservesQuantity_whenProductExists() {
        when(inventoryItemRepository.lockedFindById(productId)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem result = inventoryReservationService.reserveInventoryItem(productId, 5);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(5, result.getReserved());
    }

    @Test
    public void reserveInventoryItem_throwsInventoryItemNotFoundException_whenProductDoesNotExist() {
        when(inventoryItemRepository.lockedFindById(productId)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(InventoryItemNotFoundException.class, () -> {
            inventoryReservationService.reserveInventoryItem(productId, 5);
        });
    }

    @Test
    public void releaseInventoryItem_releasesQuantity_whenProductExists() {
        inventoryItem.reserve(5);

        when(inventoryItemRepository.lockedFindById(productId)).thenReturn(Optional.of(inventoryItem));
        when(inventoryItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryItem result = inventoryReservationService.releaseInventoryItem(productId, 4);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getReserved());
    }

    @Test
    public void releaseInventoryItem_throwsInventoryItemNotFoundException_whenProductDoesNotExist() {
        when(inventoryItemRepository.lockedFindById(productId)).thenReturn(Optional.empty());

        Assertions.assertThrowsExactly(InventoryItemNotFoundException.class, () -> {
            inventoryReservationService.releaseInventoryItem(productId, 5);
        });
    }
}
