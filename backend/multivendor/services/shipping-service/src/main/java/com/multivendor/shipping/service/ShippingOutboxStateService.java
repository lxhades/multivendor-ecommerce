package com.multivendor.shipping.service;

import com.multivendor.shipping.model.OutboxStatus;
import com.multivendor.shipping.repository.ShippingOutboxRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ShippingOutboxStateService {

    private final ShippingOutboxRepository repository;

    public ShippingOutboxStateService(
            ShippingOutboxRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public void markPublished(UUID eventId) {

        int updated = repository.markPublished(
                eventId,
                Instant.now(),
                OutboxStatus.PENDING,
                OutboxStatus.PUBLISHED
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Không thể đánh dấu sự kiện Outbox là đã gửi: " + eventId
            );
        }
    }

    @Transactional
    public void recordFailure(
            UUID eventId,
            String error,
            Instant retryAt
    ) {
        var event = repository.findById(eventId)
                .orElseThrow();

        if (event.getStatus() == OutboxStatus.PENDING) {
            event.recordFailure(error, retryAt);
        }
    }
}