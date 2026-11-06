package com.inventory_service.adapter.out.inbox;


import com.inventory_service.application.stockreservation.port.InboxPort;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.UUID;

@Repository
public class KafkaInboxJpaAdapter implements InboxPort {

    private final SpringDataKafkaInboxRepository repository;

    public KafkaInboxJpaAdapter(
            SpringDataKafkaInboxRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public boolean tryClaim(
            String consumerName,
            UUID eventId,
            String eventType
    ) {
        Objects.requireNonNull(consumerName);
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(eventType);
        int inserted = repository.insertIfAbsent(
                UUID.randomUUID(),
                consumerName,
                eventId,
                eventType
        );

        return inserted == 1;
    }

    @Override
    public boolean exists(
            String consumerName,
            UUID eventId
    ) {
        return repository.existsByConsumerNameAndEventId(
                consumerName,
                eventId
        );
    }
}