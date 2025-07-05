package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {

    @NotBlank(message = "AUTH_FULLNAME_BLANK")
    private String fullName;

    @Size(min = 10, max = 12, message = "AUTH_INVALID_PHONE")
    private String phone;
}
