package com.dmytro.orderinventoryplatform.inventory.api;

import com.dmytro.orderinventoryplatform.inventory.application.InventoryService;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST API for inventory stock levels: creating a record for a product,
 * reading it, and correcting it (stocktaking).
 *
 * <p>Reservation and release for the order lifecycle are not exposed here;
 * {@code orders} calls {@code InventoryReservationService} directly as an
 * in-process Java call, since both modules live in the same deployable.
 */
@RestController
@RequestMapping("/api/v1/inventory/items")
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    public InventoryController(InventoryService inventoryService, InventoryMapper inventoryMapper) {
        this.inventoryService = inventoryService;
        this.inventoryMapper = inventoryMapper;
    }

    /**
     * Creates an inventory record for an existing product.
     *
     * @param request the product id and starting quantity
     * @return {@code 201 Created} with the new resource's {@code Location}
     *         and body, or {@code 404} if the referenced product doesn't
     *         exist
     */
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventoryItem(@Valid @RequestBody InventoryItemCreateRequest request) {
        InventoryItem inventoryItem = inventoryService.createInventoryItem(
                request.productId(),
                request.quantity()
        );

        InventoryResponse response = inventoryMapper.toResponse(inventoryItem);

        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}");

        return ResponseEntity.created(builder.buildAndExpand(inventoryItem.getId()).toUri()).body(response);
    }

    /**
     * Looks up the inventory record for a product.
     *
     * @param productId the id of the product whose inventory is requested
     * @return {@code 200 OK} with the inventory item, or {@code 404} if no
     *         record exists for {@code productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryItem(@PathVariable Long productId) {
        InventoryItem inventoryItem = inventoryService.getInventoryItem(productId);
        InventoryResponse response = inventoryMapper.toResponse(inventoryItem);

        return ResponseEntity.ok(response);
    }

    /**
     * Corrects an inventory item's available quantity to an absolute
     * value, for example after a physical stocktake.
     *
     * @param productId the id of the product whose inventory is adjusted
     * @param request    the new absolute available quantity
     * @return {@code 200 OK} with the updated inventory item, or
     *         {@code 404} if no record exists for {@code productId}
     */
    @PatchMapping("/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjustInventoryItem(@PathVariable Long productId, @Valid @RequestBody InventoryAdjustmentRequest request) {
        InventoryItem inventoryItem = inventoryService.adjustInventoryItem(productId, request.quantity());
        InventoryResponse response = inventoryMapper.toResponse(inventoryItem);

        return ResponseEntity.ok(response);
    }
}
