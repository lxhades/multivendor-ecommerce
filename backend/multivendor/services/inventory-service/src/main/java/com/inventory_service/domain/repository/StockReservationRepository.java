package com.inventory_service.domain.repository;

import com.inventory_service.domain.model.entity.StockReservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository {

    StockReservation create(StockReservation reservation);

    StockReservation update(StockReservation reservation);

    Optional<StockReservation> findById(UUID reservationId);

    Optional<StockReservation> findByOrderIdAndInventoryId(
            UUID orderId,
            UUID inventoryId
    );

    List<StockReservation> findByOrderId(UUID orderId);
}