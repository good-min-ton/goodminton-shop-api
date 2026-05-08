package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class BrandRequest {

    @NotBlank(message = "BRAND_NAME_BLANK")
    private String name;
}
