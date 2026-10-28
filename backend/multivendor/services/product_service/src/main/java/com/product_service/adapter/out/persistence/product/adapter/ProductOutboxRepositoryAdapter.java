
package com.product_service.adapter.out.persistence.product.adapter;

import com.product_service.application.integration.port.out.ProductOutboxRepository;
import com.product_service.adapter.out.persistence.product.entity.ProductOutboxEventEntity;
import com.product_service.adapter.out.persistence.product.repository.ProductOutboxJpaRepository;

import org.springframework.stereotype.Repository;
import tools.jackson.databind.JsonNode;

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
