
package com.inventory_service.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inventories")
public class InventoryJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id", nullable = false, unique = true)
    private UUID variantId;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "on_hand_quantity", nullable = false)
    private long onHandQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private long reservedQuantity;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InventoryJpaEntity(
            UUID id,
            UUID productId,
            UUID variantId,
            UUID shopId,
            long onHandQuantity,
            long reservedQuantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.shopId = shopId;
        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = reservedQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateStock(
            long onHandQuantity,
            long reservedQuantity,
            Instant updatedAt
    ) {
        if (onHandQuantity < 0
                || reservedQuantity < 0
                || reservedQuantity > onHandQuantity) {
            throw new IllegalArgumentException(
                    "Số lượng không phù hợp"
            );
        }

        this.onHandQuantity = onHandQuantity;
        this.reservedQuantity = reservedQuantity;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
