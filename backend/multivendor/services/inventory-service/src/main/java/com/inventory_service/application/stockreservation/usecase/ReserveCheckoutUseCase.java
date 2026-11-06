package com.inventory_service.application.stockreservation.usecase;

import com.inventory_service.application.stockreservation.command.ReserveCheckoutCommand;
import com.inventory_service.application.stockreservation.event.InventoryFailed;
import com.inventory_service.application.stockreservation.event.InventoryReserved;
import com.inventory_service.application.stockreservation.port.InboxPort;
import com.inventory_service.application.stockreservation.port.ReservationOutboxPort;
import com.inventory_service.domain.model.aggregate.Inventory;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class ReserveCheckoutUseCase {

    private static final String CONSUMER_GROUP = "inventory-reservation-consumer";
    private static final String INBOX_EVENT_TYPE = "InventoryReservationRequested";
    private static final Duration RESERVATION_TIMEOUT = Duration.ofMinutes(15);

    private final InboxPort inboxPort;
    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final ReservationOutboxPort outboxPort;
    private final Clock clock;

    public ReserveCheckoutUseCase(
            InboxPort inboxPort,
            InventoryRepository inventoryRepository,
            StockReservationRepository reservationRepository,
            ReservationOutboxPort outboxPort,
            Clock clock
    ) {
        this.inboxPort = Objects.requireNonNull(inboxPort);
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
        this.outboxPort = Objects.requireNonNull(outboxPort);
        this.clock = Objects.requireNonNull(clock);
    }

    @Transactional
    public int execute(ReserveCheckoutCommand command) {
        Objects.requireNonNull(command, "command is required");

        // 1. Chống xử lý trùng event (Idempotency)
        if (!isFirstTimeProcessed(command)) {
            return -1;
        }

        Instant now = clock.instant();
        ReservationContext context = new ReservationContext();

        // 2. Chuẩn bị và kiểm tra sự tồn tại của Inventory
        Optional<String> planningError = prepareReservations(command, context);
        if (planningError.isPresent()) {
            publishReservationFailedEvent(command, planningError.get());
            return 0;
        }

        // 3. Kiểm tra số lượng tồn kho
        Optional<String> stockError = validateStockAvailability(context);
        if (stockError.isPresent()) {
            publishReservationFailedEvent(command, stockError.get());
            return 0;
        }

        // 4. Thực hiện giữ kho (Domain logic)
        applyReservations(context, now);

        // 5. Lưu thay đổi vào Database
        saveInventoryChanges(context);

        // 6. Bắn event thành công
        publishReservationSuccessEvent(command);
        return 1;

    }


    private boolean isFirstTimeProcessed(ReserveCheckoutCommand command) {
        return inboxPort.tryClaim(
                CONSUMER_GROUP,
                command.eventId(),
                INBOX_EVENT_TYPE
        );
    }

    private Optional<String> prepareReservations(ReserveCheckoutCommand command, ReservationContext context) {
        for (ReserveCheckoutCommand.Item item : command.items()) {

            Inventory inventory = inventoryRepository
                    .findByVariantId(item.variantId())
                    .orElse(null);

            if (inventory == null) {
                return Optional.of("INVENTORY_NOT_FOUND: " + item.variantId());
            }

            context.inventoryMap.put(inventory.getId(), inventory);

            var existingReservation = reservationRepository
                    .findByOrderIdAndInventoryId(item.orderId(), inventory.getId());

            if (existingReservation.isPresent()) {
                throw new IllegalStateException(
                        "Reservation already exists for order " + item.orderId()
                                + ", inventory " + inventory.getId()
                );
            }

            context.plannedReservations.add(new PlannedReservation(item, inventory));

            // Gộp nhu cầu nếu có nhiều order cùng mua một variant
            context.totalRequiredQuantities.merge(
                    inventory.getId(),
                    item.quantity(),
                    Math::addExact
            );
        }
        return Optional.empty();
    }

    private Optional<String> validateStockAvailability(ReservationContext context) {
        for (Map.Entry<UUID, Long> entry : context.totalRequiredQuantities.entrySet()) {
            Inventory inventory = context.inventoryMap.get(entry.getKey());
            long requiredQuantity = entry.getValue();

            if (inventory.availableQuantity() < requiredQuantity) {
                return Optional.of("INSUFFICIENT_STOCK: " + inventory.getVariantId());
            }
        }
        return Optional.empty();
    }

    private void applyReservations(ReservationContext context, Instant now) {
        for (PlannedReservation plan : context.plannedReservations) {
            ReserveCheckoutCommand.Item item = plan.item();
            Inventory inventory = plan.inventory();

            inventory.reserveStock(item.quantity(), now);

            StockReservation newReservation = StockReservation.create(
                    UUID.randomUUID(),
                    item.orderId(),
                    inventory.getId(),
                    item.quantity(),
                    now.plus(RESERVATION_TIMEOUT),
                    now
            );

            reservationRepository.create(newReservation);
        }
    }

    private void saveInventoryChanges(ReservationContext context) {
        for (Inventory inventory : context.inventoryMap.values()) {
            inventoryRepository.save(inventory);
        }
    }

    private void publishReservationSuccessEvent(ReserveCheckoutCommand command) {
        outboxPort.save(new InventoryReserved(
                UUID.randomUUID(),
                InventoryReserved.EVENT_TYPE,
                command.checkoutId(),
                command.eventId()
        ));
    }

    private void publishReservationFailedEvent(ReserveCheckoutCommand command, String reason) {
        outboxPort.save(new InventoryFailed(
                UUID.randomUUID(),
                InventoryFailed.EVENT_TYPE,
                command.checkoutId(),
                command.eventId(),
                reason
        ));
    }


    private static class ReservationContext {
        final List<PlannedReservation> plannedReservations = new ArrayList<>();
        final Map<UUID, Inventory> inventoryMap = new HashMap<>();
        final Map<UUID, Long> totalRequiredQuantities = new HashMap<>();
    }

    private record PlannedReservation(
            ReserveCheckoutCommand.Item item,
            Inventory inventory
    ) {}
}