
package com.inventory_service.application.stockreservation.port;

import java.util.UUID;

public interface KafkaInboxPort {
    boolean tryClaim(
            String consumerName,
            UUID eventId,
            String eventType,
            UUID checkoutId
    );
}
