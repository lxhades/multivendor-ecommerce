package com.order_service.adapter.in.web.controller;

import com.common.dto.response.ApiResponse;
import com.order_service.application.review.command.CreateReviewCommand;
import com.order_service.application.review.dto.ReviewPageResult;
import com.order_service.application.review.dto.ReviewResult;
import com.order_service.application.review.usecase.CreateReviewUseCase;
import com.order_service.application.review.usecase.ListProductReviewsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final CreateReviewUseCase create;
    private final ListProductReviewsUseCase list;

    public ReviewController(CreateReviewUseCase create, ListProductReviewsUseCase list) {
        this.create = create;
        this.list = list;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResult> create(@Valid @RequestBody CreateReviewCommand command,
                                            JwtAuthenticationToken actor) {
        if (actor == null || actor.getToken().getSubject() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT không có subject người dùng hợp lệ");
        }
        UUID buyerId;
        try { buyerId = UUID.fromString(actor.getToken().getSubject()); }
        catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT không có subject người dùng hợp lệ");
        }
        Object name = actor.getToken().getClaims().get("name");
        return ApiResponse.success("Tạo đánh giá thành công", create.execute(buyerId,
                name instanceof String text ? text : null, command));
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<ReviewPageResult> list(@PathVariable UUID productId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(list.execute(productId, page, size));
    }
}
