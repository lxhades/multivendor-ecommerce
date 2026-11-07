package com.multivendor.shipping.repository;

import com.multivendor.shipping.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByOrderId(UUID id);

    Optional<Shipment> findByTrackingCode(String code);

    List<Shipment> findByStatus(ShipmentStatus status);
}
