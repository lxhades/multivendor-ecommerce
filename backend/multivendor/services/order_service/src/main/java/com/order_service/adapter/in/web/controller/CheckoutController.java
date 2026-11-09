package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.common.exception.DomainException;
import com.common.exception.ErrorCode;
import com.order_service.adapter.in.web.request.CreateCheckoutRequest;
import com.order_service.application.checkout.dto.CheckoutResult;
import com.order_service.application.checkout.dto.CreateCheckoutResult;
import com.order_service.application.checkout.usecase.CreateCheckoutUseCase;
import com.order_service.application.checkout.usecase.GetCheckoutUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            Authentication actor
    ) {
        UUID buyerId = UUID.fromString(actor.getName());
        CreateCheckoutResult result = createCheckoutUseCase.execute(
                request.toCommand(buyerId, idempotencyKey)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo checkout thành công", result));
    }



    @GetMapping("/{id}")

    public ApiResponse<CheckoutResult> getById(@PathVariable UUID id, Authentication actor) {
        CheckoutResult result = getCheckoutUseCase.execute(id);
        boolean admin = actor.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !result.buyerId().equals(UUID.fromString(actor.getName()))) {
            throw new DomainException(ErrorCode.ACCESS_DENIED);
        }
        return ApiResponse.success(
                "Lấy thông tin checkout thành công",
                result
        );
    }
}
