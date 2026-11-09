package com.auth_service.adapters.in.web.controller;

import com.auth_service.adapters.in.web.dto.request.AddAddressRequest;
import com.auth_service.adapters.in.web.dto.request.UpdateAddressRequest;

import com.auth_service.application.command.AddAddressCommand;
import com.auth_service.application.command.DeleteAddressCommand;
import com.auth_service.application.command.UpdateAddressCommand;
import com.auth_service.application.port.in.AddressResult;
import com.auth_service.application.query.GetAllAdressesQuery;
import com.auth_service.application.usecase.AddAddressUseCase;
import com.auth_service.application.usecase.DeleteAddressUseCase;
import com.auth_service.application.usecase.GetAllAddressesUseCase;
import com.auth_service.application.usecase.UpdateAddressUseCase;
import com.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/users/{userId}/addresses")
@AllArgsConstructor
public class AddressController {

    private final AddAddressUseCase addAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final GetAllAddressesUseCase getAllAddressesUseCase;


    @PostMapping
    public ResponseEntity<ApiResponse<AddressResult>> add(
            Authentication actor,
            @PathVariable String userId,
            @Valid @RequestBody AddAddressRequest request) {

        assertCurrentUser(actor, userId);

        AddressResult result = addAddressUseCase.execute(new AddAddressCommand(
                userId,
                request.getReceiverName(),
                request.getPhone(),
                request.getLocation(),
                request.getDetail(),
                request.isDefault()
        ));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm địa chỉ thành công", result));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResult>> update(
            Authentication actor,
            @PathVariable String userId,
            @PathVariable String addressId,
            @Valid @RequestBody UpdateAddressRequest request) {

        assertCurrentUser(actor, userId);

        AddressResult result = updateAddressUseCase.execute(new UpdateAddressCommand(
                userId,
                addressId,
                request.getReceiverName(),
                request.getPhone(),
                request.getLocation(),
                request.getDetail(),
                request.isDefault()
        ));

        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công", result));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication actor,
            @PathVariable String userId,
            @PathVariable String addressId) {

        assertCurrentUser(actor, userId);

        deleteAddressUseCase.execute(new DeleteAddressCommand(userId, addressId));

        return ResponseEntity.ok(ApiResponse.success("Xóa địa chỉ thành công", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResult>>> getAllAddresses(
            Authentication actor,
            @PathVariable String userId) {

        assertCurrentUser(actor, userId);

        List<AddressResult> results = getAllAddressesUseCase.execute(
                new GetAllAdressesQuery(userId)
        );

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    private void assertCurrentUser(Authentication actor, String userId) {
        if (actor == null || !actor.getName().equals(userId)) {
            throw new AccessDeniedException("Không có quyền truy cập tài nguyên của người dùng khác");
        }
    }
}