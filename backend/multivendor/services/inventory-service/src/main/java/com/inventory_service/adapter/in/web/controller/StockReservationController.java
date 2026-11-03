package com.inventory_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.application.stockreservation.command.*;
import com.inventory_service.application.stockreservation.usecase.*;
import com.inventory_service.domain.model.entity.StockReservation;
import com.inventory_service.domain.repository.StockReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class StockReservationController {
    private final ReserveStockUseCase reserve;
    private final ConfirmReservationUseCase confirm;
    private final ReleaseReservationUseCase release;
    private final ExpireReservationUseCase expire;
    private final StockReservationRepository repository;

    public StockReservationController(ReserveStockUseCase reserve, ConfirmReservationUseCase confirm,
                                      ReleaseReservationUseCase release, ExpireReservationUseCase expire,
                                      StockReservationRepository repository) {
        this.reserve = reserve;
        this.confirm = confirm;
        this.release = release;
        this.expire = expire;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> reserve(@RequestBody ReserveStockCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Giữ hàng thành công", ReservationResponse.from(reserve.execute(command))));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Xác nhận thành công",
                ReservationResponse.from(confirm.execute(new ConfirmReservationCommand(id)))));
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<ReservationResponse>> release(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Giải phóng thành công",
                ReservationResponse.from(release.execute(new ReleaseReservationCommand(id)))));
    }

    @PostMapping("/{id}/expire")
    public ResponseEntity<ApiResponse<ReservationResponse>> expire(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Hết hạn thành công",
                ReservationResponse.from(expire.execute(new ExpireReservationCommand(id)))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> get(@PathVariable UUID id) {
        StockReservation r = repository.findById(id)
                .orElseThrow(() -> new DomainException(ErrorCode.STOCK_RESERVATION_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.from(r)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> list(@RequestParam UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(repository.findByOrderId(orderId).stream()
                .map(ReservationResponse::from).toList()));
    }

    public record ReservationResponse(UUID id, UUID orderId, UUID inventoryId, long quantity,
                                      String status, Instant expiresAt, Instant createdAt, Instant updatedAt, Long version) {
        public static ReservationResponse from(StockReservation r) {
            return new ReservationResponse(r.getId(), r.getOrderId(), r.getInventoryId(),
                    r.getQuantity(), r.getStatus().name(), r.getExpiresAt(), r.getCreatedAt(),
                    r.getUpdatedAt(), r.getVersion());
        }
    }
}
