package com.inventory_service.adapter.out.inbox;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "kafka_inbox_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_kafka_inbox_consumer_event",
                columnNames = {"consumer_name", "event_id"}
        )
)
public class KafkaInboxEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "consumer_name", nullable = false, length = 100)
    private String consumerName;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected KafkaInboxEntity() {

    }

    public KafkaInboxEntity(
            UUID id,
            String consumerName,
            UUID eventId,
            String eventType,
            Instant processedAt
    ) {
        this.id = java.util.Objects.requireNonNull(id);
        this.consumerName = java.util.Objects.requireNonNull(consumerName);
        this.eventId = java.util.Objects.requireNonNull(eventId);
        this.eventType = java.util.Objects.requireNonNull(eventType);
        this.processedAt = java.util.Objects.requireNonNull(processedAt);
    }

    public UUID getId() {
        return id;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}