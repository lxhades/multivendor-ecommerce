package com.inventory_service.application.stockreservation.port;

import com.inventory_service.application.stockreservation.event.InventoryFailed;
import com.inventory_service.application.stockreservation.event.InventoryReserved;

public interface ReservationOutboxPort {

    void save(InventoryReserved event);

    void save(InventoryFailed event);
}