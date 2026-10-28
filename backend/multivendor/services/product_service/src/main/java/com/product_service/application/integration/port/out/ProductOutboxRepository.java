package com.product_service.application.integration.port.out;

import com.product_service.application.integration.event.ProductVarientInventoryCreatedEvent;

import java.util.UUID;

public interface ProductOutboxRepository {

    void save(
            UUID eventId,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String payload
    );
}
