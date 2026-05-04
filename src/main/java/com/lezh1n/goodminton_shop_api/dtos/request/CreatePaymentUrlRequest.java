package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreatePaymentUrlRequest {

    @NotNull(message = "PAYMENT_ORDER_ID_REQUIRED")
    private Integer orderId;
}
