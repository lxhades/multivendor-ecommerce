package com.order_service.adapter.out.inbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "kafka_inbox_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_inbox_consumer_event",
                columnNames = {"consumer_name", "event_id"}
        )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
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


}