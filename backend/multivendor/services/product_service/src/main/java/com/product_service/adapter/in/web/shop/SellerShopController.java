package com.product_service.adapter.in.web.shop;

import com.product_service.adapter.in.web.Helper;
import com.product_service.adapter.in.web.dto.request.CreateShopRequest;
import com.product_service.adapter.in.web.dto.request.UpdateShopRequest;
import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.shop.command.CreateShopCommand;
import com.product_service.application.shop.command.UpdateShopCommand;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.*;
import com.product_service.domain.model.enumtype.ShopStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/seller/shops")
@RequiredArgsConstructor
public class SellerShopController {


    private final UpdateShopUseCase updateShopUseCase;
    private final CloseShopUseCase closeShopUseCase;
    private final FindShopBySellerUseCase findShopBySellerUseCase;
    private final ChangeShopStatusUseCase changeShopStatusUseCase;
    // Seller xem chi tiết shop của  mình
    @GetMapping("/my-shop")
    public ResponseEntity<ApiResponse<ShopResult>> myShopDetail(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = Helper.currentUserId(jwt);
        ShopResult result = findShopBySellerUseCase.get(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Seller cập nhật cửa hàng của mình
    @PutMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> update(
            @PathVariable UUID shopId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateShopRequest updateShopRequest) {

        UUID actorSellerId = Helper.currentUserId(jwt);

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
            @AuthenticationPrincipal Jwt jwt) {

        UUID actorSellerId = Helper.currentUserId(jwt);
        closeShopUseCase.close(shopId, actorSellerId);

        return ResponseEntity.ok(ApiResponse.success("Đóng cửa hàng thành công", null));
    }
    //Seller mo lai cua hang
    @PatchMapping("/{shopId}")
    public ResponseEntity<ApiResponse<Void>> open(
            @PathVariable UUID shopId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID actorSellerId = Helper.currentUserId(jwt);
        changeShopStatusUseCase.change(shopId, actorSellerId, ShopStatus.ACTIVE,false);

        return ResponseEntity.ok(ApiResponse.success("Mở lại cửa hàng thành công", null));
    }
}