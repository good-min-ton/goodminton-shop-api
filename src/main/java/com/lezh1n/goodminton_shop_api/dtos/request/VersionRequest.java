package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VersionRequest {

    @NotBlank(message = "VERSION_NAME_BLANK")
    private String name;
}
