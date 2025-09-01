package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrderRequest {
    @NotEmpty(message = "ORDER_ITEM_LIST_EMPTY")
    private List<OrderItemRequest> items;

    private String name;

    private String phone;

    private String address;

    private String email;

    private String note;

    @NotNull(message = "ORDER_PAYMENT_METHOD_NULL")
    private PaymentMethod paymentMethod;
}
