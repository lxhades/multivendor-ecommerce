
package com.inventory_service.application.stockreservation.usecase;

import com.inventory_service.application.stockreservation.command.*;
import com.inventory_service.domain.model.entity.StockReservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ReserveOrderStockUseCase {

    private final ReserveStockUseCase reserveStockUseCase;

    public ReserveOrderStockUseCase(
            ReserveStockUseCase reserveStockUseCase
    ) {
        this.reserveStockUseCase =
                Objects.requireNonNull(reserveStockUseCase);
    }

    @Transactional
    public List<StockReservation> execute(
            ReserveOrderStockCommand command
    ) {
        Objects.requireNonNull(command);

        List<StockReservation> results = new ArrayList<>();

        for (ReserveOrderItem item : command.items()) {

            StockReservation reservation =
                    reserveStockUseCase.execute(
                            new ReserveStockCommand(
                                    command.orderId(),
                                    item.variantId(),
                                    item.quantity()
                            )
                    );

            results.add(reservation);
        }

        return List.copyOf(results);
    }
}
