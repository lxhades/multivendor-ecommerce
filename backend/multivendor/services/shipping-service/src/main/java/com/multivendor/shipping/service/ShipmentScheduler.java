package com.multivendor.shipping.service;

import com.multivendor.shipping.dto.ShippingStatusChanged;
import com.multivendor.shipping.model.Shipment;
import com.multivendor.shipping.model.ShipmentStatus;
import com.multivendor.shipping.repository.ShipmentRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class ShipmentScheduler {

    private final ShipmentRepository shipmentRepository;
    private final ShippingOutboxService outboxService;
    private final Clock clock;

    public ShipmentScheduler(
            ShipmentRepository shipmentRepository,
            ShippingOutboxService outboxService,
            Clock clock
    ) {
        this.shipmentRepository = shipmentRepository;
        this.outboxService = outboxService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${shipping.poll-ms:500}")
    @Transactional
    public void tick() {

        Instant now = clock.instant();

        List<Shipment> shipments =
                shipmentRepository.findByStatus(
                       ShipmentStatus.SHIPPED
                );
        System.out.println("Shipments found: " + shipments.size());
        for (Shipment shipment : shipments) {

            if (!shipment.advance(now)) {
                continue;
            }

            shipmentRepository.save(shipment);

            ShippingStatusChanged event =
                    ShippingStatusChanged.delivered(
                            shipment.getOrderId(),
                            now
                    );

            outboxService.save(event);
        }
    }
}