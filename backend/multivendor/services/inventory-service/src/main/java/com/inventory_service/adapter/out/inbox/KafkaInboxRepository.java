package com.inventory_service.adapter.out.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KafkaInboxRepository
        extends JpaRepository<KafkaInboxEntity, UUID> {

    boolean existsByConsumerNameAndEventId(
            String consumerName,
            UUID eventId
    );
}
