package com.inventory_service.adapter.out.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringDataKafkaInboxRepository
        extends JpaRepository<KafkaInboxEntity, UUID> {

    boolean existsByConsumerNameAndEventId(
            String consumerName,
            UUID eventId
    );

    @Modifying
    @Query(value = """
        INSERT INTO kafka_inbox_events (
            id,
            consumer_name,
            event_id,
            event_type,
            processed_at
        )
        VALUES (
            :id,
            :consumerName,
            :eventId,
            :eventType,
            CURRENT_TIMESTAMP
        )
        ON CONFLICT (consumer_name, event_id)
        DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("consumerName") String consumerName,
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType
    );
}