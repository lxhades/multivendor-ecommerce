package com.product_service.adapter.out.persistence.product.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductOutboxJpaRepository extends JpaRepository<ProductOutboxEventEntity, UUID> {

    @Query(value = """
            SELECT * FROM product_outbox_events 
            WHERE status = :status AND retry_count < :maxRetry 
            ORDER BY created_at ASC 
            LIMIT 50 
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<ProductOutboxEventEntity> findPendingEventsForUpdate(
            @Param("status") String status,
            @Param("maxRetry") int maxRetry
    );
}