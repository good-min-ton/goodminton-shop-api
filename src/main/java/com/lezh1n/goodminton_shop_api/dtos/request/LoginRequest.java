package com.lezh1n.goodminton_shop_api.dtos.request;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String identifier;
    private String password;
}
