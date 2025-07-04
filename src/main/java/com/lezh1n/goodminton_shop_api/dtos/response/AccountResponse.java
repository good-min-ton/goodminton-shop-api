package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.UserRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse {
    private Integer accountId;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDateTime createAt;
    private AccountStatus status;
}
