package com.inventory_service.application.stockreservation.command;



import java.util.Objects;
import java.util.UUID;

public record ReleaseReservationCommand(
        UUID reservationId
) {
    public ReleaseReservationCommand {
        Objects.requireNonNull(
                reservationId,
                "reservationId is required"
        );
    }
}
