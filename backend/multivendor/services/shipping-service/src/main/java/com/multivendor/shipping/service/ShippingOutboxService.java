package com.multivendor.shipping.service;

import com.multivendor.shipping.dto.ShippingStatusChanged;
import com.multivendor.shipping.model.ShippingOutboxEvent;
import com.multivendor.shipping.repository.ShippingOutboxRepository;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Service
public class ShippingOutboxService {

    private final ShippingOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public ShippingOutboxService(
            ShippingOutboxRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void save(ShippingStatusChanged event) {

        Objects.requireNonNull(event, "Sự kiện không được để trống");

        String payload = objectMapper.writeValueAsString(event);

        repository.save(new ShippingOutboxEvent(
                event.eventId(),
                event.orderId(),
                event.eventType(),
                payload,
                event.occurredAt()
        ));
    }
}