package com.order_service.adapter.out.inbox;

import com.order_service.application.saga.port.InboxPort;
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

        return repository.insertIfAbsent(
                UUID.randomUUID(),
                consumerName,
                eventId,
                eventType
        ) == 1;
    }
}