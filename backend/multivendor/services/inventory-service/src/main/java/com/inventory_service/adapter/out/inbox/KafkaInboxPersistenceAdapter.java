
package com.inventory_service.adapter.out.inbox;

import com.inventory_service.application.stockreservation.port.KafkaInboxPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public class KafkaInboxPersistenceAdapter implements KafkaInboxPort {

    private final JdbcTemplate jdbcTemplate;

    public KafkaInboxPersistenceAdapter(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean tryClaim(
            String consumerName,
            UUID eventId,
            String eventType,
            UUID checkoutId
    ) {
        String sql = """
            INSERT INTO kafka_inbox_events (
                id,
                consumer_name,
                event_id,
                event_type,
                checkout_id,
                processed_at
            )
            VALUES (?, ?, ?, ?, ?, NOW())
            ON CONFLICT (consumer_name, event_id)
            DO NOTHING
            """;

        int inserted = jdbcTemplate.update(
                sql,
                UUID.randomUUID(),
                consumerName,
                eventId,
                eventType,
                checkoutId
        );

        return inserted == 1;
    }
}
