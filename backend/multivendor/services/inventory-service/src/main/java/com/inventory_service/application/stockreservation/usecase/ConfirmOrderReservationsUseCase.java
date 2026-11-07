package com.inventory_service.application.stockreservation.usecase;

import com.inventory_service.application.stockreservation.command.ConfirmReservationCommand;
import com.inventory_service.application.stockreservation.port.InboxPort;
import com.inventory_service.domain.model.enums.ReservationStatus;
import com.inventory_service.domain.repository.InventoryRepository;
import com.inventory_service.domain.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmOrderReservationsUseCase {

    public static final String CONSUMER_GROUP = "inventory-order-delivered-consumer";
    private static final String EVENT_TYPE = "OrderDelivered";

    private final InboxPort inboxPort;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryRepository inventoryRepository;
    private final ConfirmReservationUseCase confirmReservationUseCase;

    @Transactional
    public void execute(Event event) {
        Map<UUID, Item> eventItemsMap = indexItems(event);

        if (!inboxPort.tryClaim(CONSUMER_GROUP, event.eventId(), EVENT_TYPE)) {
            return;
        }

        var orderReservations = stockReservationRepository.findByOrderId(event.orderId());
        if (orderReservations.size() != eventItemsMap.size()) {
            throw mismatch(event.orderId());
        }

        for (var stockReservation : orderReservations) {
            var inventoryItem = inventoryRepository.findById(stockReservation.getInventoryId())
                    .orElseThrow(() -> mismatch(event.orderId()));

            var eventItem = eventItemsMap.remove(inventoryItem.getVariantId());

            if (eventItem == null
                    || !eventItem.productId().equals(inventoryItem.getProductId())
                    || !event.shopId().equals(inventoryItem.getShopId())
                    || eventItem.quantity() != stockReservation.getQuantity()) {
                throw mismatch(event.orderId());
            }

            if (stockReservation.getStatus() == ReservationStatus.RELEASED) {
                throw new IllegalStateException(
                        "Giữ chỗ đã được giải phóng: " + stockReservation.getId()
                );
            }
        }

        for (var stockReservation : orderReservations) {
            if (stockReservation.getStatus() == ReservationStatus.RESERVED) {
                confirmReservationUseCase.execute(new ConfirmReservationCommand(stockReservation.getId()));
            }
        }
    }

    private Map<UUID, Item> indexItems(Event event) {
        if (event == null
                || !EVENT_TYPE.equals(event.eventType())
                || event.eventId() == null
                || event.orderId() == null
                || event.shopId() == null
                || event.deliveredAt() == null
                || event.items() == null
                || event.items().isEmpty()) {
            throw new IllegalArgumentException("Sự kiện giao hàng thành công không hợp lệ");
        }

        Map<UUID, Item> itemMap = new HashMap<>();
        for (Item eventItem : event.items()) {
            if (eventItem == null
                    || eventItem.productId() == null
                    || eventItem.variantId() == null
                    || eventItem.quantity() <= 0
                    || itemMap.putIfAbsent(eventItem.variantId(), eventItem) != null) {
                throw new IllegalArgumentException("Dòng hàng trong sự kiện giao hàng không hợp lệ");
            }
        }

        return itemMap;
    }

    private IllegalStateException mismatch(UUID orderId) {
        return new IllegalStateException("Giữ chỗ tồn kho không khớp với đơn hàng: " + orderId);
    }

    public record Event(
            UUID eventId,
            String eventType,
            UUID orderId,
            UUID shopId,
            Instant deliveredAt,
            List<Item> items
    ) {}

    public record Item(
            UUID productId,
            UUID variantId,
            int quantity
    ) {}
}