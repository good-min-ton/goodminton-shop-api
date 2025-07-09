package com.lezh1n.goodminton_shop_api.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Authentications & Authorizations(1001 - 1100)
    AUTH_EMAIL_EXISTED(1001, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    AUTH_PHONE_EXISTED(1002, "Số điện thoại đã được sử dụng", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_CREDENTIALS(1003, "Thông tin đăng nhập không hợp lệ", HttpStatus.BAD_REQUEST),
    AUTH_UNAUTHORIZED(1004, "Unauthorized", HttpStatus.UNAUTHORIZED),
    AUTH_UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_PHONE(1006, "Số điện thoại phải có ít nhất 10 số và nhiều nhất 12 số", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_PASSWORD(1007, "Mật khẩu phải có ít nhất 8 ký tự", HttpStatus.BAD_REQUEST),
    AUTH_FULLNAME_BLANK(1008, "Họ tên không được để trống", HttpStatus.BAD_REQUEST),
    AUTH_EMAIL_BLANK(1009, "Email không được để trống", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_EMAIL(1010, "Email không đúng định dạng", HttpStatus.BAD_REQUEST),

    // Jwt & Security(1101 - 1200)
    JWT_GENERATION_ERROR(1101, "Jwt generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_INVALID_TOKEN(1102, "Invalid token", HttpStatus.BAD_REQUEST),
    JWT_EXPIRED_TOKEN(1103, "Token is expired", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_BLACKLISTED(1104, "Token has been blacklisted", HttpStatus.BAD_REQUEST),

    // Account (1201 - 1300)
    ACCOUNT_NOT_FOUND(1201, "User not found", HttpStatus.NOT_FOUND),
    ACCOUNT_OLD_PASSWORD_NOT_MATCH(1202, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    ACCOUNT_NEW_PASSWORD_SAME_AS_OLD(1203, "Mật khẩu mới không được trùng mật khẩu cũ", HttpStatus.BAD_REQUEST),

    // Store (1301 - 1400)
    STORE_NOT_FOUND(1301, "Store not found", HttpStatus.NOT_FOUND),
    STORE_ASSIGN_NON_ADMIN_ACCOUNT(1302, "You can only assign store admin account to a store",
            HttpStatus.BAD_REQUEST),
    STORE_ADMIN_ASSIGNED(1303, "Admin has been assigned with another store", HttpStatus.BAD_REQUEST),
    STORE_ADMIN_ID_REQUIRED(1304, "Store admin id required", HttpStatus.BAD_REQUEST),
    STORE_NAME_BLANK(1305, "Tên chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_ADDRESS_BLANK(1306, "Địa chỉ của chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_LOCATION_INFO_BLANK(1307, "Thông tin vị trí của chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_ADMIN_BLANK(1308, "Admin id của chi nhánh không được để trống", HttpStatus.BAD_REQUEST),
    STORE_INVENTORY_EXISTED(1309, "Cửa hàng vẫn tồn tại kho sản phẩm", HttpStatus.BAD_REQUEST),

    // Category (1401 - 1500)
    CATEGORY_NAME_REQUIRED(1401, "Tên danh mục không được để trống", HttpStatus.BAD_REQUEST),
    CATEGORY_DESCRIPTION_REQUIRED(1402, "Phần mô tả danh mục không được để trống", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1403, "Category not found", HttpStatus.BAD_REQUEST),
    CATEGORY_PRODUCT_EXISTED(1404, "Danh mục hiện đang chứa có sản phẩm", HttpStatus.BAD_REQUEST),

    // Product (1501 - 1600)

    // Brand (1601 - 1700)
    BRAND_NAME_BLANK(1601, "Tên thương hiệu không được để trống", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1602, "Không tìm thấy thương hiệu", HttpStatus.BAD_REQUEST),

    // Version (1701 - 1800)
    VERSION_NAME_BLANK(1701, "Tên phiên bản không được để trống", HttpStatus.BAD_REQUEST),
    VERSION_NOT_FOUND(1702, "Không tìm thấy phiên bản", HttpStatus.BAD_REQUEST),

    // Size (1801 - 1900)
    SIZE_NAME_BLANK(1801, "Tên size không được để trống", HttpStatus.BAD_REQUEST),
    SIZE_TYPE_BLANK(1802, "Loại size không được để trống", HttpStatus.BAD_REQUEST),
    SIZE_NOT_FOUND(1803, "Size không tồn tại", HttpStatus.BAD_REQUEST),

    // Color (1901 - 2000)
    COLOR_NAME_BLANK(1901, "Tên màu không được để trống", HttpStatus.BAD_REQUEST),
    COLOR_NOT_FOUND(1902, "Không tìm thấy màu", HttpStatus.BAD_REQUEST),

    // Data errors
    ENUM_INVALID_VALUE(9998, "Invalid enum value", HttpStatus.BAD_REQUEST),

    // System errors (9900 - 9999)
    SYSTEM_UNKNOWN_ERROR(9998, "System unknow error", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_INTERNAL_ERROR(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    private HttpStatus status;
}
