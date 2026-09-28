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
    @NotBlank(message = "UserId không được để trống")
    private String userId;
    @NotBlank(message = "AddressId không được để trống")
    private String addressId;
}
