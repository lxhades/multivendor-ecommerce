
package com.inventory_service.adapter.out.inbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "kafka_inbox_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_kafka_inbox_consumer_event",
                columnNames = {"consumer_name", "event_id"}
        )
)
public class KafkaInboxEntity {

    @Id
    private UUID id;

    @Column(name = "consumer_name", nullable = false)
    private String consumerName;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "checkout_id", nullable = false)
    private UUID checkoutId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;


}
