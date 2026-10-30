package com.inventory_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.inventory_service.application.inventory.command.InitializeInventoryCommand;
import com.inventory_service.application.inventory.dto.InventoryResponse;
import com.inventory_service.application.inventory.usecase.GetInventoryUseCase;
import com.inventory_service.application.inventory.usecase.InitializeInventoryUseCase;
import com.inventory_service.application.inventory.usecase.ListInventoryOfShopIdUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InitializeInventoryUseCase initializeUseCase;
    private final GetInventoryUseCase getUseCase;
    private final ListInventoryOfShopIdUseCase listUseCase;

    public InventoryController(
            InitializeInventoryUseCase initializeUseCase,
            GetInventoryUseCase getUseCase,
            ListInventoryOfShopIdUseCase listUseCase
    ) {
        this.initializeUseCase = initializeUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
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