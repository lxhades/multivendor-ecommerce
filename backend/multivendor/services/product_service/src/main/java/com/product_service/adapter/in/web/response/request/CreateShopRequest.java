package com.product_service.adapter.in.web.response.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateShopRequest(

        @NotBlank(message = "Tên shop không được để trống")
        @Size(max = 100, message = "Tên shop không được vượt quá 100 ký tự")
        String name,

        @NotBlank(message = "Slug không được để trống")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug chỉ chứa chữ thường, số và dấu gạch ngang")
        @Size(max = 100, message = "Slug không được vượt quá 100 ký tự")
        String slug,

        @URL(message = "Logo URL không hợp lệ")
        @Size(max = 255, message = "Logo URL quá dài")
        String logoUrl,

        @URL(message = "Banner URL không hợp lệ")
        @Size(max = 255, message = "Banner URL quá dài")
        String bannerUrl,

        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String description,

        @NotBlank(message = "Địa chỉ không được để trống")
        @Size(max = 255, message = "Địa chỉ quá dài")
        String address,

        @Size(max = 500, message = "Thông tin ngân hàng quá dài")
        String bankDetails
) {
}