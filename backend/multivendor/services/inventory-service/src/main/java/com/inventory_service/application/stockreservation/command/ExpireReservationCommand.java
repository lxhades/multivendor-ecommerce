
package com.inventory_service.application.stockreservation.command;

import java.util.Objects;
import java.util.UUID;

public record ExpireReservationCommand(
        UUID reservationId
) {
    public ExpireReservationCommand {
        Objects.requireNonNull(
                reservationId,
                "Mã giữ chỗ tồn kho không được để trống"
        );
    }
}
