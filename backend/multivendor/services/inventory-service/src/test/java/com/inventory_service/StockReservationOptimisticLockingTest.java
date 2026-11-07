
package com.inventory_service;

import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.model.enums.ReservationStatus;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockReservationOptimisticLockingTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockReservationRepository reservationRepository;

    @BeforeAll
    static void setupTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
    // khoi tao du lieu
    private StockReservation createReservation() {
        Instant now = Instant.now();

        Inventory inventory = Inventory.initialize(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now
        );

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        StockReservation reservation = StockReservation.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                savedInventory.getId(),
                5,
                now
        );

        return reservationRepository.create(reservation);
    }

    // check version=0 khi duoc tao
    @Test
    void shouldInitializeVersionToZero() {
        StockReservation reservation = createReservation();

        assertEquals(0L, reservation.getVersion());
        assertEquals(ReservationStatus.RESERVED, reservation.getStatus());
    }
    //check version sau khi update (0->1)
    @Test
    void shouldIncrementVersionAfterConfirm() {
        StockReservation reservation = createReservation();

        reservation.confirm(Instant.now());

        StockReservation updated =
                reservationRepository.update(reservation);

        assertEquals(ReservationStatus.CONFIRMED, updated.getStatus());
        assertEquals(1L, updated.getVersion());
    }

    //check throw optimisticLocking khi sai lech version
    @Test
    void shouldRejectStaleVersion() {
        StockReservation reservation = createReservation();

        UUID id = reservation.getId();

        StockReservation first =
                reservationRepository.findById(id).orElseThrow();

        StockReservation second =
                reservationRepository.findById(id).orElseThrow();

        first.confirm(Instant.now());
        reservationRepository.update(first);

        second.release(Instant.now());

        assertThrows(
                OptimisticLockingFailureException.class,
                () -> reservationRepository.update(second)
        );

        StockReservation persisted =
                reservationRepository.findById(id).orElseThrow();

        assertEquals(ReservationStatus.CONFIRMED, persisted.getStatus());
        assertEquals(1L, persisted.getVersion());
    }
}
