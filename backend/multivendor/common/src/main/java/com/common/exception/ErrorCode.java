package com.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    
    // NHÓM 1: XÁC THỰC & PHÂN QUYỀN (AUTH)
    
    AUTHENTICATION_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "Phiên đăng nhập không hợp lệ hoặc đã hết hạn"
    ),
    ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền truy cập vào tài nguyên này"
    ),
    EMAIL_NOT_FOUND(
            HttpStatus.UNAUTHORIZED,
            "Email hoặc mật khẩu không đúng"
    ),
    INVALID_PASSWORD(
            HttpStatus.UNAUTHORIZED,
            "Email hoặc mật khẩu không đúng"
    ),
    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "Phiên đăng nhập không hợp lệ hoặc đã hết hạn"
    ),
    ACCOUNT_LOCKED(
            HttpStatus.FORBIDDEN,
            "Tài khoản của bạn đã bị khóa"
    ),

    
    // NHÓM 2: NGƯỜI DÙNG & ĐỊA CHỈ (USER)
    
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy thông tin người dùng"
    ),
    ADDRESS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy thông tin địa chỉ"
    ),
    EMAIL_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Email này đã được sử dụng trên hệ thống"
    ),

    
    // NHÓM 3: CỬA HÀNG & DANH MỤC (SHOP/CATEGORY)
    
    SHOP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy shop"
    ),
    SHOP_SLUG_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Slug shop đã tồn tại"
    ),
    CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy danh mục"
    ),
    CATEGORY_SLUG_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "Slug danh mục đã tồn tại"
    ),

    
    // NHÓM 4: KHO HÀNG (INVENTORY)
    
    INVENTORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy kho hàng"
    ),
    VARIENT_INVENTORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy kho hàng của biến thể (sản phẩm)"
    ),
    INVALID_DOMAIN_RULE(
            HttpStatus.BAD_REQUEST,
            "Dữ liệu không hợp lệ theo quy tắc nghiệp vụ"
    ),
    INVALID_DISPLAY_ORDER(
            HttpStatus.BAD_REQUEST,
            "Thứ tự hiển thị không được là số âm"
    ),
    INVALID_INVENTORY_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "Số lượng tồn kho phải lớn hơn không"
    ),
    STOCK_BELOW_RESERVED_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "Tồn kho không được thấp hơn số lượng đã đặt trước"
    ),
    INSUFFICIENT_STOCK(
            HttpStatus.CONFLICT,
            "Số lượng tồn kho không đủ"
    ),

    
    // NHÓM 5: GIỮ CHỖ TỒN KHO (RESERVATION)
    
    INVALID_RESERVATION_STATUS(
            HttpStatus.CONFLICT,
            "Trạng thái giữ hàng không hợp lệ"
    ),
    STOCK_RESERVATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy bản ghi giữ chỗ tồn kho"
    ),
    ProductServiceUnavailableException(
            HttpStatus.NOT_FOUND,
            "Dịch vụ sản phẩm hiện không khả dụng"
    ),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND,"Không tìm thấy đơn hàng ");
    private final HttpStatus httpStatus;
    private final String message;
}