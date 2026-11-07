

package com.order_service.application.shipping.command;

import java.time.Instant;
import java.util.UUID;

public record HandleShippingStatusChangedCommand(
        UUID eventId,
        UUID orderId,
        String status,
        Instant occurredAt
) {}