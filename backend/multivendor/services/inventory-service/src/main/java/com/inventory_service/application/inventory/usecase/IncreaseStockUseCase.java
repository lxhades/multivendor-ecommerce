package com.inventory_service.application.inventory.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.application.inventory.command.IncreaseStockCommand;
import com.inventory_service.application.inventory.dto.InventoryResponse;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.repository.InventoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;

@Service
public class IncreaseStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final Clock clock;

    public IncreaseStockUseCase(
            InventoryRepository inventoryRepository,
            Clock clock
    ) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Transactional
    public InventoryResponse execute(IncreaseStockCommand command) {

        Inventory inventory = inventoryRepository
                .findByVariantId(command.variantId())
                .orElseThrow(() -> new DomainException(
                        ErrorCode.INVENTORY_NOT_FOUND
                ));

        inventory.increaseStock(command.quantity(), clock.instant());

        Inventory saved = inventoryRepository.save(inventory);

        return InventoryResponse.from(saved);
    }
}