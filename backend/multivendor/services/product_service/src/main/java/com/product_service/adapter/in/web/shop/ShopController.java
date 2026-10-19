package com.product_service.adapter.in.web.shop;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.product_service.adapter.in.web.response.ApiResponse;
import com.product_service.application.shop.command.*;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.*;
import com.product_service.domain.model.enumtype.ShopStatus;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private final CreateShopUseCase create;
    private final GetShopUseCase get;
    private final ListShopsUseCase list;
    private final UpdateShopUseCase update;
    private final CloseShopUseCase close;
    private final ChangeShopStatusUseCase changeStatus;

    public ShopController(
            CreateShopUseCase create,
            GetShopUseCase get,
            ListShopsUseCase list,
            UpdateShopUseCase update,
            CloseShopUseCase close,
            ChangeShopStatusUseCase changeStatus) {
        this.create = create;
        this.get = get;
        this.list = list;
        this.update = update;
        this.close = close;
        this.changeStatus = changeStatus;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShopResult>> create(@RequestBody CreateShopCommand command) {
        ShopResult result = create.create(command);

        return ResponseEntity
                .created(URI.create("/shops/" + result.id()))
                .body(ApiResponse.success("Tạo cửa hàng thành công", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShopResult>> get(@PathVariable UUID id) {
        ShopResult result = get.get(id);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShopResult>>> list(@RequestParam(required = false) UUID sellerId) {
        List<ShopResult> results = list.list(sellerId);

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    // actorSellerId is TEMPORARY for development only; replace with authenticated JWT principal.
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShopResult>> update(
            @PathVariable UUID id,
            @RequestParam UUID actorSellerId,
            @RequestBody UpdateShopCommand command) {

        ShopResult result = update.update(id, actorSellerId, command);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật cửa hàng thành công", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable UUID id,
            @RequestParam UUID actorSellerId) {

        close.close(id, actorSellerId);


        return ResponseEntity.ok(ApiResponse.success("Đóng cửa hàng thành công", null));
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShopResult>> changeStatus(
            @PathVariable UUID id,
            @RequestParam ShopStatus status) {

        ShopResult result = changeStatus.change(id, status);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái cửa hàng thành công", result));
    }
}