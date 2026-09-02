package com.dmytro.orderinventoryplatform.inventory.infrastructure;

import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link InventoryItem}.
 *
 * <p>The inherited {@link #findById} is intentionally left as a plain,
 * non-locking read - safe for concurrent viewing of stock levels.
 * {@link #lockedFindById} is a separate, explicitly-named method for
 * write paths ({@code reserve}/{@code release}/{@code adjust}), so callers
 * choose the right one rather than every read accidentally taking a lock.
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    /**
     * Locks the row with {@code PESSIMISTIC_WRITE} (a {@code SELECT ... FOR
     * UPDATE}) for the duration of the caller's transaction, so a
     * concurrent transaction attempting the same lock on the same
     * {@code productId} blocks until this one commits or rolls back.
     *
     * <p>Callers that always lock items in the same order (e.g. ascending
     * {@code productId}) when a single operation touches more than one
     * item avoid deadlocking against each other.
     *
     * @param productId the id of the inventory item (same as the product's
     *                   own id, per the shared primary key)
     * @return the inventory item, if it exists
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.id = :productId")
    Optional<InventoryItem> lockedFindById(@Param("productId") Long productId);
}
