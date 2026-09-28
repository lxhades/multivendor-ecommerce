package com.auth_service.adapters.in.web.controller;

import com.auth_service.adapters.in.web.dto.response.ApiResponse;
import com.auth_service.application.command.DeleteUserCommand;
import com.auth_service.application.port.in.AdminUserListResult;
import com.auth_service.application.port.in.AdminUserResult;
import com.auth_service.application.query.GetUserQuery;
import com.auth_service.application.query.SearchUsersQuery;
import com.auth_service.application.usecase.DeleteUserUseCase;
import com.auth_service.application.usecase.GetUserUseCase;
import com.auth_service.application.usecase.ListUsersUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public AdminUserController(GetUserUseCase getUserUseCase,
                               ListUsersUseCase listUsersUseCase,
                               DeleteUserUseCase deleteUserUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserResult>> get(@PathVariable String userId) {
        AdminUserResult result = getUserUseCase.execute(new GetUserQuery(userId));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserListResult>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AdminUserListResult result = listUsersUseCase.execute(
                new SearchUsersQuery(keyword, page, size)
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String userId) {
        deleteUserUseCase.execute(new DeleteUserCommand(userId));
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công", null));
    }
}