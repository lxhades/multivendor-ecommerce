package com.order_service.application.checkout.port;

import com.order_service.application.event.InventoryReleaseRequested;
import com.order_service.application.event.InventoryReservationRequested;

public interface OutboxPort {

    void saveInventoryReservationRequested(
            InventoryReservationRequested event
    );
    void saveInventoryReleaseRequested(
            InventoryReleaseRequested event
    );
}
