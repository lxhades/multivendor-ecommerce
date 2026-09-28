package com.auth_service.adapters.in.web.controller;

import com.auth_service.adapters.in.web.dto.request.*;
import com.auth_service.adapters.in.web.dto.response.ApiResponse;
import com.auth_service.application.command.*;
import com.auth_service.application.port.in.LoginUserResult;
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

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ChangePasswordUseCase changePasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
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
}