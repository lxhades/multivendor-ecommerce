package com.inventory_service.application.stockreservation.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.application.stockreservation.command.ReleaseReservationCommand;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.model.enums.ReservationStatus;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
public class ReleaseReservationUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final Clock clock;

    public ReleaseReservationUseCase(
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
    public StockReservation execute(
            ReleaseReservationCommand command
    ) {
        Objects.requireNonNull(command, "command is required");

        StockReservation reservation = reservationRepository
                .findById(command.reservationId())
                .orElseThrow(() -> new DomainException(
                        ErrorCode.STOCK_RESERVATION_NOT_FOUND
                ));

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return reservation;
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new DomainException(
                    ErrorCode.INVALID_RESERVATION_STATUS

            );
        }

        Instant now = clock.instant();

        reservation.release(now);

        Inventory inventory = inventoryRepository
                .findById(reservation.getInventoryId())
                .orElseThrow(() -> new DomainException(
                        ErrorCode.INVENTORY_NOT_FOUND
                ));

        inventory.releaseStock(reservation.getQuantity(), now);

        inventoryRepository.save(inventory);
        return reservationRepository.update(reservation);
    }
}
