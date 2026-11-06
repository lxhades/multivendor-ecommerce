package com.order_service.application.saga.port;

import java.util.UUID;

public interface InboxPort {

    boolean tryClaim(
            String consumerName,
            UUID eventId,
            String eventType
    );
}