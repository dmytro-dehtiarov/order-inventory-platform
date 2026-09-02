package com.dmytro.orderinventoryplatform.inventory.infrastructure;

import com.dmytro.orderinventoryplatform.catalog.domain.Category;
import com.dmytro.orderinventoryplatform.catalog.domain.Product;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.CategoryRepository;
import com.dmytro.orderinventoryplatform.catalog.infrastructure.ProductRepository;
import com.dmytro.orderinventoryplatform.inventory.domain.InventoryItem;
import com.dmytro.orderinventoryplatform.shared.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

public class InventoryItemRepositoryTest extends AbstractIntegrationTest {
    @Autowired private InventoryItemRepository inventoryItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PlatformTransactionManager transactionManager;


    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(new Category("Books", "Book category", null));
        product = productRepository.save(new Product("Product Name", "Product Description", BigDecimal.valueOf(9.99), category, true));
    }

    @AfterEach
    void tearDown() {
        inventoryItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private InventoryItem createAndSaveInventoryItem(Long productId, int quantity) {
        InventoryItem inventoryItem = new InventoryItem(productId, quantity);
        return inventoryItemRepository.save(inventoryItem);
    }

    @Test
    public void shouldSaveAndRetrieveInventoryItem() {
        InventoryItem savedItem = createAndSaveInventoryItem(product.getId(), 100);

        Assertions.assertNotNull(savedItem);
        Assertions.assertEquals(product.getId(), savedItem.getId());
    }

    @Test
    public void lockedFindById_shouldReturnInventoryItem_whenExists() {
        InventoryItem savedItem = createAndSaveInventoryItem(product.getId(), 100);
        InventoryItem lockedItem = inventoryItemRepository.lockedFindById(savedItem.getId()).orElse(null);

        Assertions.assertNotNull(lockedItem);
        Assertions.assertEquals(savedItem.getId(), lockedItem.getId());
    }

    @Test
    public void lockedFindById_shouldReturnEmpty_whenNotExists() {
        InventoryItem lockedItem = inventoryItemRepository.lockedFindById(999L).orElse(null);

        Assertions.assertNull(lockedItem);
    }

    @Test @Transactional
    public void lockedFindById_shouldBlockSecondTransaction_untilFirstCompletes() {
        InventoryItem savedItem = createAndSaveInventoryItem(product.getId(), 100);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        CountDownLatch lockAcquired = new CountDownLatch(1);
        long[] secondThreadWaitedMillis = new long[1];

        Thread threadA = new Thread(() -> {
            transactionTemplate.execute(status -> {
                inventoryItemRepository.lockedFindById(savedItem.getId()).orElse(null);
                lockAcquired.countDown();
                try {
                    Thread.sleep(500); // Simulate some processing time while holding the lock
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
        });

        Thread threadB = new Thread(() -> {
            try {
                lockAcquired.await(); // Wait for thread A to acquire the lock
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long startTime = System.currentTimeMillis();
            transactionTemplate.execute(status -> {
                inventoryItemRepository.lockedFindById(savedItem.getId()).orElse(null);
                return null;
            });
            secondThreadWaitedMillis[0] = System.currentTimeMillis() - startTime;
        });

        threadA.start();
        threadB.start();

        try {
            threadA.join(5000); // Wait for thread A to finish, with a timeout to prevent indefinite blocking
            threadB.join(5000); // Wait for thread B to finish, with a timeout to prevent indefinite blocking
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Assertions.assertTrue(secondThreadWaitedMillis[0] >= 400); // Ensure that thread B waited for at least 400ms, indicating it was blocked by thread A's lock
    }
}
