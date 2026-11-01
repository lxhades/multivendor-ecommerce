package com.inventory_service.application.inventory.usecase;

import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.application.inventory.command.AdjustStockCommand;
import com.inventory_service.application.inventory.dto.InventoryResponse;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.repository.InventoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Objects;

@Service
public class AdjustStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final Clock clock;

    public AdjustStockUseCase(
            InventoryRepository inventoryRepository,
            Clock clock
    ) {
        this.inventoryRepository = inventoryRepository;
        this.clock = clock;
    }

    @Transactional
    public InventoryResponse execute(AdjustStockCommand command) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(command.variantId());

        Inventory inventory = inventoryRepository
                .findByVariantId(command.variantId())
                .orElseThrow(() -> new DomainException(
                        ErrorCode.VARIENT_INVENTORY_NOT_FOUND
                ));

        inventory.adjustStock(
                command.newQuantity(),
                clock.instant()
        );

        Inventory saved = inventoryRepository.save(inventory);

        return InventoryResponse.from(saved);
    }
}