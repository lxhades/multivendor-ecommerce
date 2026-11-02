package com.inventory_service.adapter.out.stockreservation.persistence.entity;


import com.inventory_service.domain.model.enums.ReservationStatus;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "stock_reservations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_order_inventory",
                columnNames = {"order_id", "inventory_id"}
        ),
        indexes = {
                @Index(
                        name = "idx_reservation_order",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_reservation_status_expiry",
                        columnList = "status, expires_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockReservationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "inventory_id", nullable = false)
    private UUID inventoryId;

    @Column(name = "quantity", nullable = false)
    private long quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public StockReservationJpaEntity(
            UUID id,
            UUID orderId,
            UUID inventoryId,
            long quantity,
            ReservationStatus status,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.inventoryId = inventoryId;
        this.quantity = quantity;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateStatus(
            ReservationStatus status,
            Instant updatedAt
    ) {
        this.status = status;
        this.updatedAt = updatedAt;
    }
}