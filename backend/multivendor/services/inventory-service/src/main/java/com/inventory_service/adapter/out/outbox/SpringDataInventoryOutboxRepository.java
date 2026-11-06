package com.inventory_service.adapter.out.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataInventoryOutboxRepository
        extends JpaRepository<InventoryOutboxEntity, UUID> {

    @Query("""
        SELECT e FROM InventoryOutboxEntity e
        WHERE e.status = :status
          AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
        ORDER BY e.createdAt ASC
        """)
    List<InventoryOutboxEntity> findReadyEvents(
            @Param("status") InventoryOutboxStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE InventoryOutboxEntity e
        SET e.status = :published,
            e.publishedAt = :now,
            e.nextRetryAt = NULL,
            e.lastError = NULL
        WHERE e.id = :id
          AND e.status = :pending
        """)
    @Transactional
    int markPublished(
            @Param("id") UUID id,
            @Param("now") Instant now,
            @Param("pending") InventoryOutboxStatus pending,
            @Param("published") InventoryOutboxStatus published
    );

    @Modifying
    @Query("""
        UPDATE InventoryOutboxEntity e
        SET e.attemptCount = e.attemptCount + 1,
            e.nextRetryAt = :nextRetryAt,
            e.lastError = :error
        WHERE e.id = :id
          AND e.status = :pending
        """)
    @Transactional
    int recordFailure(
            @Param("id") UUID id,
            @Param("nextRetryAt") Instant nextRetryAt,
            @Param("error") String error,
            @Param("pending") InventoryOutboxStatus pending
    );
}