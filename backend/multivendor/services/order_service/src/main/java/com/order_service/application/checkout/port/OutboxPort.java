package com.order_service.application.checkout.port;

import com.order_service.application.event.InventoryReleaseRequested;
import com.order_service.application.event.InventoryReservationRequested;
import com.order_service.application.event.OrderShipped;

public interface OutboxPort {

    void saveInventoryReservationRequested(
            InventoryReservationRequested event
    );
    void saveInventoryReleaseRequested(
            InventoryReleaseRequested event
    );
    void saveOrderShipped(OrderShipped event);
}
