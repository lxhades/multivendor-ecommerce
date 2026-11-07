package com.multivendor.shipping.repository;

import com.multivendor.shipping.model.OutboxStatus;
import com.multivendor.shipping.model.ShippingOutboxEvent;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ShippingOutboxRepository
        extends JpaRepository<ShippingOutboxEvent, UUID> {

    @Query("""
        SELECT e
        FROM ShippingOutboxEvent e
        WHERE e.status = :status
          AND e.nextRetryAt <= :now
        ORDER BY e.createdAt ASC
    """)
    List<ShippingOutboxEvent> findReadyEvents(
            @Param("status") OutboxStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE ShippingOutboxEvent e
        SET e.status = :published,
            e.publishedAt = :now
        WHERE e.id = :id
          AND e.status = :pending
    """)
    int markPublished(
            @Param("id") UUID id,
            @Param("now") Instant now,
            @Param("pending") OutboxStatus pending,
            @Param("published") OutboxStatus published
    );
}