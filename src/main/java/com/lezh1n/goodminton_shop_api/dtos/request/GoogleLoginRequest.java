package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GoogleLoginRequest {

    /**
     * The ID token from Google Identity Services. Named after the field the
     * browser library puts it in, so the two sides read the same.
     */
    @NotBlank(message = "AUTH_GOOGLE_TOKEN_INVALID")
    private String credential;
}
