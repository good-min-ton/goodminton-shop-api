package com.lezh1n.goodminton_shop_api.dtos.response;

import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private UserRole role;
    private AccountStatus status;
}
