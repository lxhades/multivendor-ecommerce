package com.order_service.adapter.out.outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrderOutboxRepository
        extends JpaRepository<OrderOutboxEntity, UUID> {

    // Must be called inside the transaction that publishes and updates the event.
    @Query(value = """
            SELECT * FROM order_outbox_events
            WHERE status = 'PENDING' AND event_type = :eventType
            ORDER BY created_at ASC, id ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<OrderOutboxEntity> findNextPendingForUpdate(
            @Param("eventType") String eventType
    );

    List<OrderOutboxEntity> findTop100ByStatusOrderByCreatedAtAsc(
            OutboxStatus status
    );
}
