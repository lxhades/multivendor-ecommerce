package com.product_service.adapter.in.web.shop;

import com.product_service.adapter.in.web.dto.request.CreateShopRequest;
import com.product_service.adapter.in.web.dto.request.UpdateShopRequest;
import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.shop.command.CreateShopCommand;
import com.product_service.application.shop.command.UpdateShopCommand;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.*;
import jakarta.validation.Valid; // <-- Thêm import này
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final CreateShopUseCase createShopUseCase;
    private final GetShopUseCase getShopUseCase;
    private final ListShopsUseCase listShopsUseCase;
    private final UpdateShopUseCase updateShopUseCase;
    private final CloseShopUseCase closeShopUseCase;
    private final ChangeShopStatusUseCase changeShopStatusUseCase;
    private final FindShopBySellerUseCase findShopBySellerUseCase;


    private UUID currentUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalStateException("JWT không có subject");
        }
        return UUID.fromString(sub);
    }

    // User tạo cửa hàng
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'SELLER')")
    public ResponseEntity<ApiResponse<ShopResult>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateShopRequest createShopRequest) {

        UUID userId = currentUserId(jwt);


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

    // Xem chi tiết cửa hàng (Dùng chung)
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> findById(@PathVariable UUID shopId) {
        ShopResult result = getShopUseCase.get(shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // Seller xem chi tiết shop của mình
    @GetMapping("/myShop")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ShopResult>> myShopDetail(@AuthenticationPrincipal Jwt jwt) {
        UUID shopId = currentUserId(jwt);
        ShopResult result = findShopBySellerUseCase.get(shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShopResult>>> list(Pageable pageable) {
        Page<ShopResult> shopResultPage = listShopsUseCase.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(shopResultPage));
    }

    // Seller cập nhật cửa hàng của mình
    @PutMapping("/{shopId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ShopResult>> update(
            @PathVariable UUID shopId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateShopRequest updateShopRequest) {
        UUID actorSellerId = currentUserId(jwt);


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
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable UUID shopId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID actorSellerId = currentUserId(jwt);
        closeShopUseCase.close(shopId, actorSellerId);

        return ResponseEntity.ok(ApiResponse.success("Đóng cửa hàng thành công", null));
    }


}