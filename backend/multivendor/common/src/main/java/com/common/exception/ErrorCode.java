package com.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập vào tài nguyên này"),
    EMAIL_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin địa chỉ"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email này đã được sử dụng trên hệ thống"),
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy shop"),
    SHOP_SLUG_ALREADY_EXISTS(HttpStatus.CONFLICT, "Slug shop đã tồn tại"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"),
    CATEGORY_SLUG_ALREADY_EXISTS(HttpStatus.CONFLICT, "Slug danh mục đã tồn tại"),
    INVALID_DOMAIN_RULE(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ theo quy tắc nghiệp vụ"),
    INVALID_DISPLAY_ORDER(HttpStatus.BAD_REQUEST, "Thứ tự hiển thị không được là số âm"),
    INVALID_INVENTORY_QUANTITY(HttpStatus.BAD_REQUEST, "Số lượng tồn kho phải lớn hơn không"),
    STOCK_BELOW_RESERVED_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "Tồn kho không được thấp hơn số lượng đã đặt trước"
    );

    private final HttpStatus httpStatus;
    private final String message;
}
