package com.inventory_service.application.inventory.usecase;

import com.inventory_service.application.inventory.command.InitializeInventoryCommand;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class InitializeInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final Clock clock;

    public InitializeInventoryUseCase(
            InventoryRepository inventoryRepository,
            Clock clock
    ) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Transactional
    public Inventory execute(InitializeInventoryCommand command) {

        Objects.requireNonNull(command, "Command không được để trống");

        UUID productId = Objects.requireNonNull(
                command.productId(), "productId không được để trống"
        );

        UUID variantId = Objects.requireNonNull(
                command.variantId(), "variantId không được để trống"
        );

        UUID shopId = Objects.requireNonNull(
                command.shopId(), "shopId không được để trống"
        );

        var existing = inventoryRepository.findByVariantId(variantId);

        if (existing.isPresent()) {
            Inventory inventory = existing.get();

            if (!inventory.getProductId().equals(productId)
                    || !inventory.getShopId().equals(shopId)) {

                throw new IllegalStateException(
                        "Biến thể này đã thuộc về một sản phẩm hoặc cửa hàng khác"
                );
            }

            return inventory;
        }

        Instant now = clock.instant();

        Inventory inventory = Inventory.initialize(
                UUID.randomUUID(),
                productId,
                variantId,
                shopId,
                now
        );

        return inventoryRepository.save(inventory);
    }
}