package com.order_service.application.checkout.port;

import com.order_service.application.checkout.event.InventoryReservationRequested;

public interface OutboxPort {

    void saveInventoryReservationRequested(
            InventoryReservationRequested event
    );
}
