package com.lezh1n.goodminton_shop_api.dto.request;

import lombok.Getter;

@Getter
public class LogoutRequest {
    private String accessToken;
    private String refreshToken;
}
