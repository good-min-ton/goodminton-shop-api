package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateAccountRequest {

    @NotBlank(message = "AUTH_FULLNAME_BLANK")
    private String fullName;

    @NotBlank(message = "AUTH_EMAIL_BLANK")
    @Email
    private String email;

    @Size(min = 10, max = 12, message = "AUTH_INVALID_PHONE")
    private String phone;

    @Size(min = 8, message = "AUTH_INVALID_PASSWORD")
    private String password;
}
