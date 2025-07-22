package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSpecificationRequest {
    @NotBlank(message = "SPEC_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "SPEC_VALUE_REQUIRED")
    private String value;
}
