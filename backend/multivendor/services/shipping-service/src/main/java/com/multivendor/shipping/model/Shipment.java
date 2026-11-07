package com.multivendor.shipping.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "shipments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "order_id"),
                @UniqueConstraint(columnNames = "tracking_code")
        }
)
public class Shipment {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "tracking_code", nullable = false)
    private String trackingCode;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Version
    private Long version;

    protected Shipment() {
    }

    public Shipment(UUID orderId, String address, Instant now) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.trackingCode = "SHIP-" + id;
        this.shippingAddress = address;
        this.createdAt = now;
        this.status = ShipmentStatus.PENDING;
    }

    public static Shipment fromOrderShipped(UUID orderId, String shippingAddress, Instant shippedAt) {
        Shipment shipment = new Shipment(orderId, shippingAddress, shippedAt);
        shipment.status = ShipmentStatus.SHIPPED;
        shipment.shippedAt = shippedAt;
        return shipment;
    }

    public boolean advance(Instant now) {
        if (status == ShipmentStatus.PENDING && !now.isBefore(createdAt.plusSeconds(3))) {
            status = ShipmentStatus.SHIPPED;
            shippedAt = now;
            return true;
        }

        if (status == ShipmentStatus.SHIPPED && !now.isBefore(shippedAt.plusSeconds(3))) {
            status = ShipmentStatus.DELIVERED;
            deliveredAt = now;
            return true;
        }

        return false;
    }


    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}