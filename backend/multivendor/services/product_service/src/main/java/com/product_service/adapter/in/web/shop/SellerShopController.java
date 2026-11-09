package com.product_service.adapter.in.web.shop;

import com.product_service.adapter.in.web.Helper;
import com.product_service.adapter.in.web.dto.request.CreateShopRequest;
import com.product_service.adapter.in.web.dto.request.UpdateShopRequest;
import com.common.dto.response.ApiResponse;
import com.product_service.application.shop.command.CreateShopCommand;
import com.product_service.application.shop.command.UpdateShopCommand;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.*;
import com.product_service.domain.model.enumtype.ShopStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/shops/seller")

@RequiredArgsConstructor
public class SellerShopController {


    private final UpdateShopUseCase updateShopUseCase;
    private final CloseShopUseCase closeShopUseCase;
    private final FindShopBySellerUseCase findShopBySellerUseCase;
    private final ChangeShopStatusUseCase changeShopStatusUseCase;

    // Seller xem chi tiết shop của  mình
    private final CreateShopUseCase createShopUseCase;

    // Seller tạo cửa hàng
    @PostMapping
    public ResponseEntity<ApiResponse<ShopResult>> create(
            Authentication actor,
            @Valid @RequestBody CreateShopRequest createShopRequest) {

        UUID userId = Helper.currentUserId(actor);

        CreateShopCommand createShopCommand = new CreateShopCommand(
                createShopRequest.name(),
                createShopRequest.slug(),
                createShopRequest.logoUrl(),
                createShopRequest.bannerUrl(),
                createShopRequest.description(),
                createShopRequest.address(),
                createShopRequest.bankDetails()
        );

        ShopResult createdShopResult = createShopUseCase.create(userId, createShopCommand);

        return ResponseEntity
                .created(URI.create("/shops/" + createdShopResult.id()))
                .body(ApiResponse.success("Tạo cửa hàng thành công", createdShopResult));
    }

    @GetMapping("/my-shop")
    public ResponseEntity<ApiResponse<ShopResult>> myShopDetail(Authentication actor) {
        UUID userId = Helper.currentUserId(actor);
        ShopResult result = findShopBySellerUseCase.get(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Seller cập nhật cửa hàng của mình
    @PutMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> update(
            @PathVariable UUID shopId,
            Authentication actor,
            @Valid @RequestBody UpdateShopRequest updateShopRequest) {

        UUID actorSellerId = Helper.currentUserId(actor);

        UpdateShopCommand updateShopCommand = new UpdateShopCommand(
                updateShopRequest.name(),
                updateShopRequest.slug(),
                updateShopRequest.logoUrl(),
                updateShopRequest.bannerUrl(),
                updateShopRequest.description(),
                updateShopRequest.address(),
                updateShopRequest.bankDetails()
        );

        ShopResult updatedShopResult = updateShopUseCase.update(shopId, actorSellerId, updateShopCommand);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật cửa hàng thành công", updatedShopResult));
    }

    // Seller đóng cửa hàng
    @DeleteMapping("/{shopId}")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable UUID shopId,
            Authentication actor) {

        UUID actorSellerId = Helper.currentUserId(actor);
        closeShopUseCase.close(shopId, actorSellerId);

        return ResponseEntity.ok(ApiResponse.success("Đóng cửa hàng thành công", null));
    }
    //Seller mo lai cua hang
    @PatchMapping("/{shopId}")
    public ResponseEntity<ApiResponse<Void>> open(
            @PathVariable UUID shopId,
            Authentication actor) {

        UUID actorSellerId = Helper.currentUserId(actor);
        changeShopStatusUseCase.change(shopId, actorSellerId, ShopStatus.ACTIVE,false);

        return ResponseEntity.ok(ApiResponse.success("Mở lại cửa hàng thành công", null));
    }
}