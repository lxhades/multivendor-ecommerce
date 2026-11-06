
package com.order_service.adapter.out.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataOrderOutboxRepository
        extends JpaRepository<OrderOutboxEntity, UUID> {

    @Query("""
        SELECT e
        FROM OrderOutboxEntity e
        WHERE e.status = :status
          AND (
              e.nextRetryAt IS NULL
              OR e.nextRetryAt <= :now
          )
        ORDER BY e.createdAt ASC
        """)
    List<OrderOutboxEntity> findReadyEvents(
            @Param("status") OutboxStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );


    @Modifying
    @Transactional
    @Query("""
        UPDATE OrderOutboxEntity e
        SET e.status = :published,
            e.publishedAt = :now,
            e.nextRetryAt = NULL,
            e.lastError = NULL
        WHERE e.id = :id
          AND e.status = :pending
        """)
    int markPublished(
            @Param("id") UUID id,
            @Param("now") Instant now,
            @Param("pending") OutboxStatus pending,
            @Param("published") OutboxStatus published
    );


    @Modifying
    @Transactional
    @Query("""
        UPDATE OrderOutboxEntity e
        SET e.attemptCount = e.attemptCount + 1,
            e.nextRetryAt = :nextRetryAt,
            e.lastError = :error
        WHERE e.id = :id
          AND e.status = :pending
        """)
    int recordFailure(
            @Param("id") UUID id,
            @Param("nextRetryAt") Instant nextRetryAt,
            @Param("error") String error,
            @Param("pending") OutboxStatus pending
    );


}
