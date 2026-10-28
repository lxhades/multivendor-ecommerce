package com.product_service.adapter.in.web.shop;

import com.product_service.adapter.in.web.Helper;
import com.product_service.adapter.in.web.dto.request.CreateShopRequest;
import com.product_service.adapter.in.web.dto.response.ApiResponse;
import com.product_service.application.shop.command.CreateShopCommand;
import com.product_service.application.shop.result.ShopResult;
import com.product_service.application.shop.usecase.CreateShopUseCase;
import com.product_service.application.shop.usecase.GetShopUseCase;
import com.product_service.application.shop.usecase.ListShopsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class PublicShopController {

    private final GetShopUseCase getShopUseCase;
    private final ListShopsUseCase listShopsUseCase;
    private final CreateShopUseCase createShopUseCase;

    // Seller tạo cửa hàng
    @PostMapping
    public ResponseEntity<ApiResponse<ShopResult>> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateShopRequest createShopRequest) {

        UUID userId = Helper.currentUserId(jwt);

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

    // Xem danh sách cửa hàng (Phân trang)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ShopResult>>> list(Pageable pageable) {
        Page<ShopResult> shopResultPage = listShopsUseCase.list(pageable);
        return ResponseEntity.ok(ApiResponse.success(shopResultPage));
    }

    // Xem chi tiết cửa hàng theo ID
    @GetMapping("/{shopId}")
    public ResponseEntity<ApiResponse<ShopResult>> findById(@PathVariable UUID shopId) {
        ShopResult result = getShopUseCase.get(shopId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}