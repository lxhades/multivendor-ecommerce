
package com.inventory_service;

import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.repository.InventoryRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private InventoryRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeAll
    static void setupTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    @Test
    void shouldPreventLostUpdate() throws Exception {

        Inventory inventory = Inventory.initialize(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now()
        );

        UUID inventoryId = repository.save(inventory).getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch bothRead = new CountDownLatch(2);
        CountDownLatch startUpdate = new CountDownLatch(1);

        try {
            Callable<Boolean> task1 = () ->
                    updateStock(inventoryId, 10, bothRead, startUpdate);

            Callable<Boolean> task2 = () ->
                    updateStock(inventoryId, 20, bothRead, startUpdate);

            Future<Boolean> future1 = executor.submit(task1);
            Future<Boolean> future2 = executor.submit(task2);

            assertTrue(
                    bothRead.await(10, TimeUnit.SECONDS),
                    "Both transactions must read Inventory"
            );

            startUpdate.countDown();

            boolean result1 = future1.get(15, TimeUnit.SECONDS);
            boolean result2 = future2.get(15, TimeUnit.SECONDS);

            assertNotEquals(
                    result1,
                    result2,
                    "Exactly one transaction should succeed"
            );

            Inventory persisted =
                    repository.findById(inventoryId).orElseThrow();

            long expectedQuantity = result1 ? 10L : 20L;

            assertEquals(
                    expectedQuantity,
                    persisted.getOnHandQuantity()
            );

            assertEquals(1L, persisted.getVersion());

        } finally {
            startUpdate.countDown();
            executor.shutdownNow();
        }
    }

    private boolean updateStock(
            UUID inventoryId,
            long quantity,
            CountDownLatch bothRead,
            CountDownLatch startUpdate
    ) {
        try {
            transactionTemplate.executeWithoutResult(status -> {

                Inventory inventory =
                        repository.findById(inventoryId).orElseThrow();

                bothRead.countDown();

                try {
                    if (!startUpdate.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Timeout waiting for concurrent update"
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }

                    inventory.increaseStock(quantity, Instant.now());

                    repository.save(inventory);
            });

            return true;

        } catch (OptimisticLockingFailureException ex) {
            return false;
        }
    }
}
