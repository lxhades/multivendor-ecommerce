
package com.product_service.adapter.out.persistence.product.outbox;

import com.product_service.application.integration.port.out.ProductOutboxRepository;

import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Repository
public class ProductOutboxRepositoryAdapter
        implements ProductOutboxRepository {

    private final ProductOutboxJpaRepository jpaRepository;
    private final Clock clock;

    public ProductOutboxRepositoryAdapter(
            ProductOutboxJpaRepository jpaRepository,
            Clock clock
    ) {
        this.jpaRepository = jpaRepository;
        this.clock = clock;
    }

    @Override
    public void save(
            UUID eventId,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String payload
    ) {
        ProductOutboxEventEntity entity = new ProductOutboxEventEntity(
                        eventId,
                        aggregateType,
                        aggregateId,
                        eventType,
                        payload,
                        Instant.now(clock)
                );

        jpaRepository.save(entity);
    }
}
