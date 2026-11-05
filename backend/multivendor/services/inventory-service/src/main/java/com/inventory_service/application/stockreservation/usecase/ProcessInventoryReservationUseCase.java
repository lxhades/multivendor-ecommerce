
package com.inventory_service.application.stockreservation.usecase;

import com.inventory_service.application.stockreservation.command.ReserveCheckoutStockCommand;
import com.inventory_service.application.stockreservation.port.KafkaInboxPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class ProcessInventoryReservationUseCase {

    private static final String CONSUMER_NAME =
            "inventory-reservation-consumer";

    private static final String EVENT_TYPE =
            "InventoryReservationRequested";

    private final KafkaInboxPort inboxPort;
    private final ReserveCheckoutStockUseCase reserveCheckoutStockUseCase;

    public ProcessInventoryReservationUseCase(
            KafkaInboxPort inboxPort,
            ReserveCheckoutStockUseCase reserveCheckoutStockUseCase
    ) {
        this.inboxPort = inboxPort;
        this.reserveCheckoutStockUseCase =
                reserveCheckoutStockUseCase;
    }

    @Transactional
    public boolean execute(
            UUID eventId,
            ReserveCheckoutStockCommand command
    ) {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(command);

        // ktra event
        boolean claimed = inboxPort.tryClaim(
                CONSUMER_NAME,
                eventId,
                EVENT_TYPE,
                command.checkoutId()
        );

        if (!claimed) {
            return false;
        }

        // Giữ hàng toàn bộ Checkout.
        reserveCheckoutStockUseCase.execute(command);

        return true;
    }
}
