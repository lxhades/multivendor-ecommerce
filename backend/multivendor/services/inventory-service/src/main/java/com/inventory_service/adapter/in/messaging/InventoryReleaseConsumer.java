package com.inventory_service.adapter.in.messaging;

import com.inventory_service.application.stockreservation.usecase.ReleaseOrderReservationsUseCase;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
public class InventoryReleaseConsumer {

    private final ObjectMapper mapper;
    private final ReleaseOrderReservationsUseCase releaseOrderReservationsUseCase;

    public InventoryReleaseConsumer(
            ObjectMapper mapper,
            ReleaseOrderReservationsUseCase useCase
    ) {
        this.mapper = mapper;
        this.releaseOrderReservationsUseCase = useCase;
    }

    @KafkaListener(
            topics = "${inventory.kafka.release-topic}",
            groupId = "inventory-release-consumer"
    )
    public void consume(String jsonPayload) {

        Event event = mapper.readValue(jsonPayload, Event.class);

        if (!"InventoryReleaseRequested".equals(event.eventType())
                || event.eventId() == null
                || event.orderId() == null) {
            throw new IllegalArgumentException("Invalid release event");
        }

        releaseOrderReservationsUseCase.execute(event.eventId(), event.orderId());
    }

    public record Event(
            UUID eventId,
            String eventType,
            UUID orderId
    ) {}
}