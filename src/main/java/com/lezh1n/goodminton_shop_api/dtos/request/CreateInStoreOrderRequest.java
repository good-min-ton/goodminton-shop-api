package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateInStoreOrderRequest {

    @Size(max = 100)
    private String customerName;

    @Size(max = 20)
    private String customerPhone;

    @NotEmpty(message = "ORDER_ITEMS_REQUIRED")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "ORDER_PAYMENT_METHOD_REQUIRED")
    private PaymentMethod paymentMethod;
}
