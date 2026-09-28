package com.auth_service.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddAddressRequest {
    @NotBlank @Size(max = 100) private String receiverName;
    @NotBlank @Pattern(regexp = "^0[0-9]{9}$") private String phone;
    @NotBlank @Size(max = 255) private String location;
    @NotBlank @Size(max = 500) private String detail;
    private boolean isDefault;
}
