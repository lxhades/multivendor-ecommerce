package com.inventory_service.application.stockreservation.command;

import java.util.Objects;
import java.util.UUID;

public record ConfirmReservationCommand(
        UUID reservationId
) {
    public ConfirmReservationCommand {
        Objects.requireNonNull(
                reservationId,
                "Mã giữ chỗ tồn kho không được để trống"
        );
    }
}