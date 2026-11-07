package com.inventory_service.application.stockreservation.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.application.stockreservation.command.ReserveStockCommand;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class ReserveStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final Clock clock;

    public ReserveStockUseCase(
            InventoryRepository inventoryRepository,
            StockReservationRepository reservationRepository,
            Clock clock
    ) {
        this.inventoryRepository =
                Objects.requireNonNull(inventoryRepository);
        this.reservationRepository =
                Objects.requireNonNull(reservationRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Transactional
    public StockReservation execute(ReserveStockCommand command) {

        Objects.requireNonNull(command, "Yêu cầu xử lý không được để trống");

        Inventory inventory = inventoryRepository
                .findByVariantId(command.variantId())
                .orElseThrow(() -> new DomainException(
                        ErrorCode.INVENTORY_NOT_FOUND
                ));

        var existing = reservationRepository
                .findByOrderIdAndInventoryId(
                        command.orderId(),
                        inventory.getId()
                );

        if (existing.isPresent()) {
            StockReservation reservation = existing.get();

            if (reservation.getQuantity() != command.quantity()) {
                throw new IllegalStateException(
                        "Đã có bản ghi giữ chỗ với số lượng khác"
                );
            }

            return reservation;
        }

        Instant now = clock.instant();

        inventory.reserveStock(command.quantity(), now);

        StockReservation reservation = StockReservation.create(
                UUID.randomUUID(),
                command.orderId(),
                inventory.getId(),
                command.quantity(),
                now
        );

        inventoryRepository.save(inventory);

        return reservationRepository.create(reservation);
    }
}
