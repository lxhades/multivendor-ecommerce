package com.product_service.adapter.in.web.dto.request;


import jakarta.validation.constraints.NotBlank;

public record ChangeStatusCategoryRequest(
        @NotBlank(message = "Trạng thái không được để trống")
        String status
) {
}