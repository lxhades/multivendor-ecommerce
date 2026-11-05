package com.order_service.adapter.out.outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOrderOutboxRepository
        extends JpaRepository<OrderOutboxEntity, UUID> {

    List<OrderOutboxEntity> findTop100ByStatusOrderByCreatedAtAsc(
            OutboxStatus status
    );
}