package com.auth_service.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAddressRequest {
    @NotBlank(message = "Mã người dùng không được để trống")
    private String userId;
    @NotBlank(message = "Mã địa chỉ không được để trống")
    private String addressId;
}
