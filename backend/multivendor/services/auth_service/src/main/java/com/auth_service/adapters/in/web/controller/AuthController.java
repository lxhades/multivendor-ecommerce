package com.auth_service.adapters.in.web.controller;

import com.auth_service.adapters.in.web.dto.request.*;

import com.auth_service.adapters.in.web.dto.response.ApiResponse;
import com.auth_service.application.command.*;
import com.auth_service.application.port.in.LoginUserResult;
import com.auth_service.application.port.in.RefreshTokenResult;
import com.auth_service.application.port.in.RegisterUserResult;
import com.auth_service.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ChangePasswordUseCase changePasswordUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUserResult>> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterUserCommand command = new RegisterUserCommand(
                request.getEmail(),
                request.getPhone(),
                request.getPassword()
        );

        RegisterUserResult result = registerUserUseCase.execute(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginUserResult>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginUserCommand command = new LoginUserCommand(
                request.getEmail(),
                request.getPassword()
        );

        LoginUserResult result = loginUserUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", result));
    }

    @PutMapping("/forgotpassword")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotpasswordRequest forgotpasswordRequest) {

        ForgotPasswordCommand command = new ForgotPasswordCommand(
                forgotpasswordRequest.getEmail()
        );

        forgotPasswordUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Mật khẩu mới đã được gửi về mail của bạn", null)
        );
    }

    @PutMapping("/changepassword")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {

        ChangePasswordCommand command = new ChangePasswordCommand(
                changePasswordRequest.getUserId(),
                changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword()
        );

        changePasswordUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success("Đổi mật khẩu thành công", null)
        );
    }
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResult> refresh(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest
    ) {
        RefreshTokenCommand command =
                new RefreshTokenCommand(refreshTokenRequest.refreshToken());

        RefreshTokenResult result =
                refreshTokenUseCase.execute(command);

        return ApiResponse.success(
                "Làm mới access token thành công",
                result
        );
    }
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        LogoutCommand command =
                new LogoutCommand(request.refreshToken());

        logoutUseCase.execute(command);

        return ApiResponse.success(
                "Đăng xuất thành công",
                null
        );
    }
}