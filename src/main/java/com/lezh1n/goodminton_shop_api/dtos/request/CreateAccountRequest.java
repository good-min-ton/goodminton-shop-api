package com.lezh1n.goodminton_shop_api.dtos.request;

import lombok.Getter;

@Getter
public class CreateAccountRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
}
