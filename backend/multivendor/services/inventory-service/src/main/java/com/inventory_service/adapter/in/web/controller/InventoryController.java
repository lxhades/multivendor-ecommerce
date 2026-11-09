package com.inventory_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.inventory_service.adapter.out.inventory.http.ShopOwnershipClient;
import com.inventory_service.application.inventory.command.AdjustStockCommand;
import com.inventory_service.application.inventory.command.IncreaseStockCommand;
import com.inventory_service.application.inventory.command.InitializeInventoryCommand;
import com.inventory_service.application.inventory.dto.InventoryResponse;
import com.inventory_service.application.inventory.usecase.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class InventoryController {

    private final InitializeInventoryUseCase initializeUseCase;
    private final GetInventoryUseCase getUseCase;
    private final ListInventoryOfShopIdUseCase listUseCase;
    private final IncreaseStockUseCase  increaseStockUseCase;
    private final AdjustStockUseCase adjustStockUseCase;
    private final ShopOwnershipClient ownership;

    @PatchMapping("/inventories/admin/increase-stock")

    public ResponseEntity<ApiResponse<InventoryResponse>> increaseStock(
            @RequestBody IncreaseStockCommand request
    ) {
        InventoryResponse response = increaseStockUseCase.execute(
                new IncreaseStockCommand(request.variantId(), request.quantity())
        );

        return ResponseEntity.ok(
                ApiResponse.success("Nhập kho thành công", response)
        );
    }

    @PatchMapping("/inventories/seller/variants/{variantId}/stock")

    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(
            @PathVariable UUID variantId,
            @jakarta.validation.Valid @RequestBody AdjustStockRequest request,
            Authentication actor
    ) {
        requireOwner(actor, getUseCase.getByVariantId(variantId).shopId());
        InventoryResponse response = adjustStockUseCase.execute(
                new AdjustStockCommand(
                        variantId,
                        request.newQuantity()
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Điều chỉnh tồn kho thành công",
                        response
                )
        );
    }



    @PostMapping("/inventories/admin")

    public ResponseEntity<ApiResponse<InventoryResponse>> initialize(
            @RequestBody InitializeInventoryCommand command
    ) {
        var inventory = initializeUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Khởi tạo tồn kho thành công",
                        InventoryResponse.from(inventory))
                );
    }

    @GetMapping("/inventories/variant/{variantId}")
    public ApiResponse<AvailableStock> available(@PathVariable UUID variantId) {
        var stock = getUseCase.getByVariantId(variantId);
        return ApiResponse.success(new AvailableStock(stock.variantId(), stock.availableQuantity()));
    }

    @GetMapping("/inventories/seller/{id}")

    public ApiResponse<InventoryResponse> sellerInventory(@PathVariable UUID id, Authentication actor) {
        var stock = getUseCase.getById(id);
        requireOwner(actor, stock.shopId());
        return ApiResponse.success(stock);
    }

    @GetMapping("/inventories/seller/variants/{variantId}")

    public ApiResponse<InventoryResponse> sellerVariant(@PathVariable UUID variantId, Authentication actor) {
        var stock = getUseCase.getByVariantId(variantId);
        requireOwner(actor, stock.shopId());
        return ApiResponse.success(stock);
    }

    @GetMapping("/inventories/seller")

    public ApiResponse<List<InventoryResponse>> sellerInventories(@RequestParam UUID shopId, Authentication actor) {
        requireOwner(actor, shopId);
        return ApiResponse.success(listUseCase.execute(shopId));
    }

    @GetMapping("/inventories/admin/{id}")

    public ApiResponse<InventoryResponse> adminInventory(@PathVariable UUID id) {
        return ApiResponse.success(getUseCase.getById(id));
    }

    @GetMapping("/inventories/admin/variants/{variantId}")

    public ApiResponse<InventoryResponse> adminVariant(@PathVariable UUID variantId) {
        return ApiResponse.success(getUseCase.getByVariantId(variantId));
    }

    @GetMapping("/inventories/admin")

    public ApiResponse<List<InventoryResponse>> adminInventories(@RequestParam UUID shopId) {
        return ApiResponse.success(listUseCase.execute(shopId));
    }

    private void requireOwner(Authentication actor, UUID shopId) {
        if (!ownership.isOwner(UUID.fromString(actor.getName()), shopId)) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }
    }

    public record AdjustStockRequest(
            @jakarta.validation.constraints.NotNull(message = "Số lượng tồn kho không được để trống")
            Long newQuantity
    ) {}

    public record AvailableStock(UUID variantId, long availableQuantity) {}
}
