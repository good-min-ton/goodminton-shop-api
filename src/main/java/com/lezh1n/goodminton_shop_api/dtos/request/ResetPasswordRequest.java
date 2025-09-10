package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    @NotBlank(message = "ACCOUNT_RESET_TOKEN_BLANK")
    private String token;

    @Size(min = 8, message = "AUTH_INVALID_PASSWORD")
    private String newPassword;
}
