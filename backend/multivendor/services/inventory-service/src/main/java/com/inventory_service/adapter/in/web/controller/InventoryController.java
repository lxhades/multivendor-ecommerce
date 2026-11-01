package com.inventory_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.inventory_service.application.inventory.command.AdjustStockCommand;
import com.inventory_service.application.inventory.command.IncreaseStockCommand;
import com.inventory_service.application.inventory.command.InitializeInventoryCommand;
import com.inventory_service.application.inventory.dto.InventoryResponse;
import com.inventory_service.application.inventory.usecase.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InitializeInventoryUseCase initializeUseCase;
    private final GetInventoryUseCase getUseCase;
    private final ListInventoryOfShopIdUseCase listUseCase;
    private final IncreaseStockUseCase  increaseStockUseCase;
    private final AdjustStockUseCase adjustStockUseCase;

    @PatchMapping("/increase-stock")
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
    @PatchMapping("/variant/adjust-stock")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(
            @RequestBody AdjustStockCommand request
    ) {
        InventoryResponse response = adjustStockUseCase.execute(
                new AdjustStockCommand(
                        request.variantId(),
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



    @PostMapping
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getById(@PathVariable UUID id) {
        InventoryResponse response = getUseCase.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getByVariantId(
            @PathVariable UUID variantId
    ) {
        InventoryResponse response = getUseCase.getByVariantId(variantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getByShopId(
            @RequestParam UUID shopId
    ) {
        List<InventoryResponse> responses = listUseCase.execute(shopId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}