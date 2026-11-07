
package com.inventory_service;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.application.stockreservation.command.ReserveStockCommand;
import com.inventory_service.application.stockreservation.usecase.ReserveStockUseCase;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.model.enums.ReservationStatus;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReserveStockIntegrationTest {

    @Autowired
    private ReserveStockUseCase reserveStockUseCase;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockReservationRepository reservationRepository;

    @BeforeAll
    static void setupTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    private Inventory createInventory(long quantity) {
        Instant now = Instant.now();

        Inventory inventory = Inventory.initialize(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now
        );

        inventory.increaseStock(quantity, now);

        return inventoryRepository.save(inventory);
    }

    private Inventory reloadInventory(UUID inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow();
    }

    // T1: Reserve thành công
    @Test
    void shouldReserveStockSuccessfully() {

        Inventory inventory = createInventory(100);
        UUID orderId = UUID.randomUUID();

        StockReservation reservation =
                reserveStockUseCase.execute(
                        new ReserveStockCommand(
                                orderId,
                                inventory.getVariantId(),
                                5
                        )
                );

        Inventory updated = reloadInventory(inventory.getId());

        assertEquals(100L, updated.getOnHandQuantity());
        assertEquals(5L, updated.getReservedQuantity());
        assertEquals(95L, updated.availableQuantity());

        assertEquals(ReservationStatus.RESERVED,
                reservation.getStatus());
        assertEquals(5L, reservation.getQuantity());
        assertEquals(orderId, reservation.getOrderId());
        assertEquals(inventory.getId(), reservation.getInventoryId());

        StockReservation persisted = reservationRepository
                .findById(reservation.getId())
                .orElseThrow();

        assertEquals(ReservationStatus.RESERVED,
                persisted.getStatus());
    }

    // T2: Không đủ hàng
    @Test
    void shouldRejectWhenStockIsInsufficient() {

        Inventory inventory = createInventory(100);
        UUID orderId = UUID.randomUUID();

        DomainException exception = assertThrows(
                DomainException.class,
                () -> reserveStockUseCase.execute(
                        new ReserveStockCommand(
                                orderId,
                                inventory.getVariantId(),
                                120
                        )
                )
        );

        assertEquals(ErrorCode.INSUFFICIENT_STOCK,
                exception.getErrorCode());

        Inventory updated = reloadInventory(inventory.getId());

        assertEquals(100L, updated.getOnHandQuantity());
        assertEquals(0L, updated.getReservedQuantity());

        assertTrue(reservationRepository
                .findByOrderIdAndInventoryId(
                        orderId, inventory.getId()
                ).isEmpty());
    }

    // T3: Gửi lại cùng yêu cầu
    @Test
    void shouldNotReserveTwiceForSameOrder() {

        Inventory inventory = createInventory(100);
        UUID orderId = UUID.randomUUID();

        ReserveStockCommand command = new ReserveStockCommand(
                orderId,
                inventory.getVariantId(),
                10
        );

        StockReservation first =
                reserveStockUseCase.execute(command);

        StockReservation second =
                reserveStockUseCase.execute(command);

        Inventory updated = reloadInventory(inventory.getId());

        assertEquals(first.getId(), second.getId());
        assertEquals(10L, updated.getReservedQuantity());
        assertEquals(90L, updated.availableQuantity());

        assertEquals(1, reservationRepository
                .findByOrderId(orderId).size());
    }

    // T4: Cùng order nhưng số lượng khác
    @Test
    void shouldRejectDifferentQuantityForSameOrder() {

        Inventory inventory = createInventory(100);
        UUID orderId = UUID.randomUUID();

        reserveStockUseCase.execute(
                new ReserveStockCommand(
                        orderId,
                        inventory.getVariantId(),
                        5
                )
        );

        assertThrows(
                IllegalStateException.class,
                () -> reserveStockUseCase.execute(
                        new ReserveStockCommand(
                                orderId,
                                inventory.getVariantId(),
                                10
                        )
                )
        );

        Inventory updated = reloadInventory(inventory.getId());

        assertEquals(5L, updated.getReservedQuantity());
        assertEquals(95L, updated.availableQuantity());
    }

    // T5: Quantity không hợp lệ
    @Test
    void shouldRejectZeroQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ReserveStockCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        0
                )
        );
    }

    // T6: Variant không tồn tại
    @Test
    void shouldRejectUnknownVariant() {

        DomainException exception = assertThrows(
                DomainException.class,
                () -> reserveStockUseCase.execute(
                        new ReserveStockCommand(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                5
                        )
                )
        );

        assertEquals(ErrorCode.INVENTORY_NOT_FOUND,
                exception.getErrorCode());
    }

    // T7: Reservation INSERT lỗi -> Inventory rollback
    @Test
    void shouldRollbackInventoryWhenReservationInsertFails() {

        Inventory inventory = createInventory(100);
        UUID orderId = UUID.randomUUID();

        // Tạo Reservation trước, nhưng không tăng reservedQuantity.
        // Chỉ dùng để thiết lập tình huống kiểm thử.
        StockReservation existing = StockReservation.create(
                UUID.randomUUID(),
                orderId,
                inventory.getId(),
                5,
                Instant.now()
        );

        reservationRepository.create(existing);

        // Với UseCase hiện tại, quantity khác nhau
        // sẽ bị từ chối trước khi ghi Inventory.
        assertThrows(
                IllegalStateException.class,
                () -> reserveStockUseCase.execute(
                        new ReserveStockCommand(
                                orderId,
                                inventory.getVariantId(),
                                10
                        )
                )
        );

        Inventory updated = reloadInventory(inventory.getId());

        assertEquals(0L, updated.getReservedQuantity());
        assertEquals(100L, updated.getOnHandQuantity());
    }
}
