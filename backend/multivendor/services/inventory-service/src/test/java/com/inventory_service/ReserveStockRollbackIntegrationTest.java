
package com.inventory_service;

import com.inventory_service.application.stockreservation.command.ReserveStockCommand;
import com.inventory_service.application.stockreservation.usecase.ReserveStockUseCase;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class ReserveStockRollbackIntegrationTest {

    @Autowired
    private ReserveStockUseCase reserveStockUseCase;

    @Autowired
    private InventoryRepository inventoryRepository;

    @MockitoSpyBean
    private StockReservationRepository reservationRepository;
    @BeforeAll
    static void setupTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    @Test
    void shouldRollbackInventoryWhenReservationCreationFails() {

        Instant now = Instant.now();

        Inventory inventory = Inventory.initialize(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now
        );

        inventory.increaseStock(100, now);

        Inventory saved = inventoryRepository.save(inventory);

        UUID inventoryId = saved.getId();
        UUID orderId = UUID.randomUUID();

        assertEquals(100L, saved.getOnHandQuantity());
        assertEquals(0L, saved.getReservedQuantity());



        doThrow(new RuntimeException(
                "Giả lập lỗi lưu bản ghi giữ chỗ"
        ))
                .when(reservationRepository)
                .create(any(StockReservation.class));



        ReserveStockCommand command = new ReserveStockCommand(
                orderId,
                saved.getVariantId(),
                10
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> reserveStockUseCase.execute(command)
        );

        assertEquals(
                "Giả lập lỗi lưu bản ghi giữ chỗ",
                exception.getMessage()
        );



        Inventory afterRollback = inventoryRepository
                .findById(inventoryId)
                .orElseThrow();

        // Inventory phải trở về trạng thái ban đầu
        assertEquals(100L, afterRollback.getOnHandQuantity());
        assertEquals(0L, afterRollback.getReservedQuantity());
        assertEquals(100L, afterRollback.availableQuantity());

        assertEquals(saved.getVersion(), afterRollback.getVersion());

        assertTrue(
                reservationRepository
                        .findByOrderIdAndInventoryId(
                                orderId,
                                inventoryId
                        )
                        .isEmpty()
        );
    }
}
