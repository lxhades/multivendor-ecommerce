package com.inventory_service.application.stockreservation.command;



import java.util.Objects;
import java.util.UUID;

public record ReleaseReservationCommand(
        UUID reservationId
) {
    public ReleaseReservationCommand {
        Objects.requireNonNull(
                reservationId,
                "Mã giữ chỗ tồn kho không được để trống"
        );
    }
}
