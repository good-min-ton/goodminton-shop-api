package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    private String oldPassword;

    @Size(min = 8, message = "AUTH_INVALID_PASSWORD")
    private String newPassword;
    
    private String confirmPassword;
}
