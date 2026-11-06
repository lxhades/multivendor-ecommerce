package com.inventory_service.adapter.out.outbox;

import com.inventory_service.application.stockreservation.event.InventoryFailed;
import com.inventory_service.application.stockreservation.event.InventoryReserved;
import com.inventory_service.application.stockreservation.port.ReservationOutboxPort;

import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@Repository
public class InventoryOutboxJpaAdapter implements ReservationOutboxPort {

    private final SpringDataInventoryOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public InventoryOutboxJpaAdapter(
            SpringDataInventoryOutboxRepository repository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void save(InventoryReserved event) {
        saveEvent(
                event.eventId(),
                event.checkoutId(),
                InventoryReserved.EVENT_TYPE,
                event
        );
    }

    @Override
    public void save(InventoryFailed event) {
        saveEvent(
                event.eventId(),
                event.checkoutId(),
                InventoryFailed.EVENT_TYPE,
                event
        );
    }

    private void saveEvent(
            UUID eventId,
            UUID checkoutId,
            String eventType,
            Object event
    ) {
        String payload = objectMapper.writeValueAsString(event);

        InventoryOutboxEntity entity = new InventoryOutboxEntity(
                eventId,
                checkoutId,
                eventType,
                payload,
                clock.instant()
        );

        repository.save(entity);
    }
}