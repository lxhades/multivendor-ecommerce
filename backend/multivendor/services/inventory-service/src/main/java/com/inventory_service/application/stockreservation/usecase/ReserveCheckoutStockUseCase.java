
package com.inventory_service.application.stockreservation.usecase;

import com.inventory_service.application.stockreservation.command.*;
import com.inventory_service.domain.model.entity.StockReservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReserveCheckoutStockUseCase {

    private final ReserveStockUseCase reserveStockUseCase;

    public ReserveCheckoutStockUseCase(
            ReserveStockUseCase reserveStockUseCase
    ) {
        this.reserveStockUseCase = reserveStockUseCase;
    }

    @Transactional
    public List<StockReservation> execute(
            ReserveCheckoutStockCommand command
    ) {

        return command.items().stream()
                .map(item -> reserveStockUseCase.execute(
                        new ReserveStockCommand(
                                item.orderId(),
                                item.variantId(),
                                item.quantity()
                        )
                ))
                .toList();


    }
}
