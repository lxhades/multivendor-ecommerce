package com.product_service.adapter.in.web.shop;

import com.product_service.adapter.in.web.response.ApiResponse;
import com.product_service.application.shop.command.CreateShopCommand;
import com.product_service.application.shop.command.UpdateShopCommand;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.*;
import com.product_service.domain.model.enumtype.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/shops")
public class ShopController {


    private final CreateShopUseCase createShopUseCase;
    private final GetShopUseCase getShopUseCase;
    private final ListShopsUseCase listShopsUseCase;
    private final UpdateShopUseCase updateShopUseCase;
    private final CloseShopUseCase closeShopUseCase;
    private final ChangeShopStatusUseCase changeShopStatusUseCase;

    public ShopController(
            CreateShopUseCase createShopUseCase,
            GetShopUseCase getShopUseCase,
            ListShopsUseCase listShopsUseCase,
            UpdateShopUseCase updateShopUseCase,
            CloseShopUseCase closeShopUseCase,
            ChangeShopStatusUseCase changeShopStatusUseCase) {
        this.createShopUseCase = createShopUseCase;
        this.getShopUseCase = getShopUseCase;
        this.listShopsUseCase = listShopsUseCase;
        this.updateShopUseCase = updateShopUseCase;
        this.closeShopUseCase = closeShopUseCase;
        this.changeShopStatusUseCase = changeShopStatusUseCase;
    }

    // User tạo cửa hàng
    @PostMapping
    public ResponseEntity<ApiResponse<ShopResult>> create(@RequestParam UUID userId, @RequestBody CreateShopCommand createShopCommand) {
        ShopResult createdShopResult = createShopUseCase.create(userId, createShopCommand);

        return ResponseEntity
                .created(URI.create("/shops/" + createdShopResult.id()))
                .body(ApiResponse.success("Tạo cửa hàng thành công", createdShopResult));
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> findById(@PathVariable UUID shopId) {
        ShopResult result = getShopUseCase.get(shopId);

        return ResponseEntity.ok(ApiResponse.success(result));

    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShopResult>>> list(
            @RequestParam(required = false) UUID sellerId,
            Pageable pageable) {

        Page<ShopResult> shopResultPage = listShopsUseCase.list(sellerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(shopResultPage));
    }

    @PutMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> update(
            @PathVariable UUID shopId,
            @RequestParam UUID actorSellerId,
            @RequestBody UpdateShopCommand updateShopCommand) {

        ShopResult updatedShopResult = updateShopUseCase.update(shopId, actorSellerId, updateShopCommand);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật cửa hàng thành công", updatedShopResult));
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable UUID shopId,
            @RequestParam UUID actorSellerId) {

        closeShopUseCase.close(shopId, actorSellerId);

        return ResponseEntity.ok(ApiResponse.success("Đóng cửa hàng thành công", null));
    }

    @PatchMapping("/{shopId}/status")
    public ResponseEntity<ApiResponse<ShopResult>> changeStatus(
            @PathVariable UUID shopId,
            @RequestParam ShopStatus shopStatus) {

        ShopResult updatedShopResult = changeShopStatusUseCase.change(shopId, shopStatus);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái cửa hàng thành công", updatedShopResult));
    }
}