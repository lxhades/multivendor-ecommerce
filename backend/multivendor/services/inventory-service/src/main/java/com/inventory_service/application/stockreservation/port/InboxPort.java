package com.inventory_service.application.stockreservation.port;

import java.util.UUID;

public interface InboxPort {

    boolean tryClaim(
            String consumerName,
            UUID eventId,
            String eventType

    );

    boolean exists(
            String consumerName,
            UUID eventId
    );
}