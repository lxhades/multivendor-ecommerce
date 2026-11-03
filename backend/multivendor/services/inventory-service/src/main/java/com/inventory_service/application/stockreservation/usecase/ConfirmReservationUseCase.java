
package com.inventory_service.application.stockreservation.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.application.stockreservation.command.ConfirmReservationCommand;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.model.enums.ReservationStatus;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ConfirmReservationUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final Clock clock;


    @Transactional
    public StockReservation execute(
            ConfirmReservationCommand command
    ) {
        Objects.requireNonNull(command, "command is required");


        StockReservation reservation = reservationRepository
                .findById(command.reservationId())
                .orElseThrow(() -> new DomainException(
                        ErrorCode.STOCK_RESERVATION_NOT_FOUND
                ));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return reservation;
        }

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException(
                    "Cannot confirm reservation in status: "
                            + reservation.getStatus()
            );
        }

        Instant now = clock.instant();


        reservation.confirm(now);

        Inventory inventory = inventoryRepository
                .findById(reservation.getInventoryId())
                .orElseThrow(() -> new DomainException(
                        ErrorCode.INVENTORY_NOT_FOUND
                ));

        inventory.confirmStock(reservation.getQuantity(), now);

        inventoryRepository.save(inventory);

        return reservationRepository.update(reservation);
    }
}
