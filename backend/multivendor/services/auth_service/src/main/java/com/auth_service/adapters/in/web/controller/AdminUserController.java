package com.auth_service.adapters.in.web.controller;


import com.auth_service.adapters.in.web.dto.request.AdminUpdateUserRequest;
import com.auth_service.adapters.in.web.dto.request.AdminUserStatusRequest;
import com.auth_service.application.usecase.ManageAdminUserUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import com.auth_service.application.port.in.AdminUserListResult;
import com.auth_service.application.port.in.AdminUserResult;
import com.auth_service.application.query.GetUserQuery;
import com.auth_service.application.query.SearchUsersQuery;
import com.auth_service.application.usecase.GetUserUseCase;
import com.auth_service.application.usecase.ListUsersUseCase;
import com.common.dto.response.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/users/admin")
@AllArgsConstructor
public class AdminUserController {

    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final ManageAdminUserUseCase manageUser;



    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResult>> get(@PathVariable String userId, Authentication actor) {
        manageUser.requireActiveAdmin(actor.getName());
        AdminUserResult result = getUserUseCase.execute(new GetUserQuery(userId));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserListResult>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, Authentication actor) {
        manageUser.requireActiveAdmin(actor.getName());

        AdminUserListResult result = listUsersUseCase.execute(
                new SearchUsersQuery(keyword, page, size)
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String userId, Authentication actor) {
        manageUser.delete(actor.getName(), userId);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResult>> update(@PathVariable String userId,
            @Valid @RequestBody AdminUpdateUserRequest request, Authentication actor) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật người dùng thành công",
                manageUser.update(actor.getName(), userId, request.email(), request.phone())));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserResult>> status(@PathVariable String userId,
            @Valid @RequestBody AdminUserStatusRequest request, Authentication actor) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái người dùng thành công",
                manageUser.changeStatus(actor.getName(), userId, request.status())));
    }
}