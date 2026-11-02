
package com.inventory_service;

import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.repository.InventoryRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InventoryOptimisticLockingTest {

    @Autowired
    private InventoryRepository repository;
    @BeforeAll
    static void setupTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    @Test
    void shouldRejectStaleInventoryUpdate() {

        Inventory inventory = Inventory.initialize(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now()
        );

        Inventory saved = repository.save(inventory);

        Inventory first =
                repository.findById(saved.getId()).orElseThrow();

        Inventory second =
                repository.findById(saved.getId()).orElseThrow();

        first.increaseStock(10, Instant.now());
        repository.save(first);

        second.increaseStock(20, Instant.now());

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> repository.save(second)
        );

        Inventory persisted =
                repository.findById(saved.getId()).orElseThrow();

        assertEquals(10L, persisted.getOnHandQuantity());
        assertEquals(1L, persisted.getVersion());
    }
}
