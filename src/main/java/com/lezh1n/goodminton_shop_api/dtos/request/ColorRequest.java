package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ColorRequest {

    @NotBlank(message = "COLOR_NAME_BLANK")
    private String name;
}
