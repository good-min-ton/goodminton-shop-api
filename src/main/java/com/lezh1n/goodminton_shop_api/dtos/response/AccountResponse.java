package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.AuthProvider;
import com.lezh1n.goodminton_shop_api.enums.UserRole;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDateTime createdAt;
    private AccountStatus status;
    private AuthProvider provider;
    private String avatarUrl;
    /**
     * Whether a password exists at all — never the password or its hash.
     *
     * Not the same question as {@code provider == LOCAL}: someone who signed up
     * through Google can still set a password afterwards via "forgot password",
     * and stays a GOOGLE account. The UI needs the real answer to decide between
     * offering "change password" and "set a password".
     */
    private Boolean hasPassword;
}
