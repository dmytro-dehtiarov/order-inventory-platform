package com.dmytro.orderinventoryplatform.inventory.api;

import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InventoryMapperTest {

    @Test
    public void toResponse_mapsAllFields() {
        InventoryMapper inventoryMapper = new InventoryMapper();
        InventoryItem inventoryItem = new InventoryItem(1L, 10);
        inventoryItem.reserve(4);
        InventoryResponse response = inventoryMapper.toResponse(inventoryItem);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(inventoryItem.getId(), response.id());
        Assertions.assertEquals(inventoryItem.getAvailable(), response.availableQuantity());
        Assertions.assertEquals(inventoryItem.getReserved(), response.reservedQuantity());
        Assertions.assertEquals(inventoryItem.getAvailable() + inventoryItem.getReserved(), response.totalQuantity());
    }
}
