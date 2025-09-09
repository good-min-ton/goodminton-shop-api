package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ForgotPasswordRequest {
    @NotBlank(message = "AUTH_EMAIL_BLANK")
    @Email(message = "AUTH_INVALID_EMAIL")
    private String email;
}
