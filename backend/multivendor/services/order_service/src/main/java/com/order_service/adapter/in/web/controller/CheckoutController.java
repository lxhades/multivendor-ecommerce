package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.order_service.adapter.in.web.request.CreateCheckoutRequest;
import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.application.checkout.dto.CreateCheckoutResult;
import com.order_service.application.checkout.usecase.CreateCheckoutUseCase;
import com.order_service.application.checkout.usecase.GetCheckoutUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutController {

    private final CreateCheckoutUseCase createCheckoutUseCase;
    private final GetCheckoutUseCase getCheckoutUseCase;

    public CheckoutController(
            CreateCheckoutUseCase createCheckoutUseCase,
            GetCheckoutUseCase getCheckoutUseCase
    ) {
        this.createCheckoutUseCase = createCheckoutUseCase;
        this.getCheckoutUseCase = getCheckoutUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCheckoutResult>> create(
            @Valid @RequestBody CreateCheckoutRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID buyerId = UUID.fromString(jwt.getSubject());

        CreateCheckoutResult result = createCheckoutUseCase.execute(
                request.toCommand(buyerId, idempotencyKey)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo checkout thành công", result));
    }



    @GetMapping("/{id}")
    public ApiResponse<CheckoutResult> getById(@PathVariable UUID id) {
        return ApiResponse.success(
                "Lấy thông tin checkout thành công",
                getCheckoutUseCase.execute(id)
        );
    }
}