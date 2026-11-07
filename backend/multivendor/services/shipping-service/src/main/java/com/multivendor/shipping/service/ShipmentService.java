package com.multivendor.shipping.service;

import com.multivendor.shipping.dto.*;
import com.multivendor.shipping.model.*;
import com.multivendor.shipping.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

@Service
public class ShipmentService {
    private final ShipmentRepository repo;
    private final Clock clock;

    public ShipmentService(ShipmentRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional
    public ShipmentResponse create(CreateShipmentRequest request) {

        var existing = repo.findByOrderId(request.orderId());
        if (existing.isPresent()) {
            if (!existing.get().getShippingAddress().equals(request.shippingAddress()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Địa chỉ giao hàng khác với địa chỉ đã lưu");
            return ShipmentResponse.from(existing.get());
        }
        return ShipmentResponse.from(repo.saveAndFlush(new Shipment(request.orderId(), request.shippingAddress(), clock.instant())));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse byTracking(String code) {
        return ShipmentResponse.from(repo.findByTrackingCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin vận chuyển")));
    }

    @Transactional(readOnly = true)
    public ShipmentResponse byOrder(UUID id) {
        return ShipmentResponse.from(repo.findByOrderId(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin vận chuyển")));
    }

    @Transactional
    public void createFromOrderShipped(OrderShippedEvent event) {

        Objects.requireNonNull(event, "Sự kiện không được để trống");
        Objects.requireNonNull(event.orderId(), "Mã đơn hàng không được để trống");

        if (event.shippingAddress() == null
                || event.shippingAddress().isBlank()) {
            throw new IllegalArgumentException(
                    "Địa chỉ giao hàng không được để trống"
            );
        }

        var existing = repo.findByOrderId(event.orderId());

        if (existing.isPresent()) {
            if (!existing.get().getShippingAddress()
                    .equals(event.shippingAddress())) {
                throw new IllegalStateException(
                        "Dữ liệu vận chuyển không khớp cho đơn hàng "
                                + event.orderId()
                );
            }
            return;
        }

        Shipment shipment = Shipment.fromOrderShipped(
                event.orderId(),
                event.shippingAddress(),
                clock.instant()
        );

        repo.saveAndFlush(shipment);
    }
}
