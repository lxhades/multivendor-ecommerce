package com.product_service.adapter.in.web.dto.request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.util.UUID;

public record CreateCategoryRequest(


        UUID parentId,

        @NotBlank(message = "Tên danh mục không được để trống")
        @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
        String name,

        @NotBlank(message = "Slug không được để trống")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ chứa chữ thường, số và dấu gạch ngang")
        @Size(max = 100, message = "Slug không được vượt quá 100 ký tự")
        String slug,

        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String description,

        @URL(message = "Image URL không hợp lệ")
        @Size(max = 255, message = "Image URL quá dài")
        String imageUrl,


        @Null
        @Min(value = 0, message = "Thứ tự hiển thị không được là số âm")
        Integer displayOrder
) {
}