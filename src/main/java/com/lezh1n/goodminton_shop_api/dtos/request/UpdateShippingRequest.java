package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateShippingRequest {

    @NotBlank(message = "ORDER_SHIPPING_CODE_REQUIRED")
    private String shippingCode;
}
