package com.lezh1n.goodminton_shop_api.exceptions;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Authentications & Authorizations(1000 - 1100)
    EMAIL_EXISTED(1001, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(1002, "Số điện thoại đã được sử dụng", HttpStatus.BAD_REQUEST),
    AUTH_UNAUTHORIZED(1003, "Unauthorized", HttpStatus.UNAUTHORIZED),
    // Account (1101 - 1200)

    // Data errors
    ENUM_INVALID_VALUE(9998, "Invalid enum value", HttpStatus.BAD_REQUEST),
    // System errors (9900 - 9999)
    SYSTEM_UNKNOWN_ERROR(9998, "System unknow error", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_INTERNAL_ERROR(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private int code;
    private String message;
    private HttpStatus status;
}
