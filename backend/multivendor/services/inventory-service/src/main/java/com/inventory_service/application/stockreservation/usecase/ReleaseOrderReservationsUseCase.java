package com.inventory_service.application.stockreservation.usecase;

import com.inventory_service.application.stockreservation.command.ReleaseReservationCommand;
import com.inventory_service.application.stockreservation.port.InboxPort;
import com.inventory_service.domain.model.enums.ReservationStatus;
import com.inventory_service.domain.repository.StockReservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReleaseOrderReservationsUseCase {

    private final InboxPort inbox;
    private final StockReservationRepository reservations;
    private final ReleaseReservationUseCase release;

    public ReleaseOrderReservationsUseCase(
            InboxPort inbox,
            StockReservationRepository reservations,
            ReleaseReservationUseCase release
    ) {
        this.inbox = inbox;
        this.reservations = reservations;
        this.release = release;
    }

    @Transactional
    public void execute(UUID eventId, UUID orderId) {

        var records = reservations.findByOrderId(orderId);


        if (records.isEmpty()) {
            throw new IllegalStateException(
                    "Reservations not found: " + orderId
            );
        }

        boolean claimed = inbox.tryClaim(
                "inventory-release-consumer",
                eventId,
                "InventoryReleaseRequested"
        );

        if (!claimed) {
            return;
        }

        for (var reservation : records) {
            if (reservation.getStatus() == ReservationStatus.RELEASED) {
                continue;
            }

            if (reservation.getStatus() != ReservationStatus.RESERVED) {
                throw new IllegalStateException(
                        "Cannot release reservation: "
                                + reservation.getId()
                );
            }

            release.execute(
                    new ReleaseReservationCommand(
                            reservation.getId()
                    )
            );
        }
    }
}