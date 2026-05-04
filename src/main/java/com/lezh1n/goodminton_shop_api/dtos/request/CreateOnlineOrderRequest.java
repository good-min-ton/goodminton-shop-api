package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import com.lezh1n.goodminton_shop_api.enums.PaymentMethod;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateOnlineOrderRequest {

    @NotEmpty(message = "ORDER_ITEMS_REQUIRED")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "ORDER_RECIPIENT_NAME_REQUIRED")
    private String recipientName;

    @NotBlank(message = "ORDER_RECIPIENT_PHONE_REQUIRED")
    private String recipientPhone;

    @NotBlank(message = "ORDER_RECIPIENT_ADDRESS_REQUIRED")
    private String recipientAddress;

    @Email(message = "ORDER_RECIPIENT_EMAIL_INVALID")
    private String recipientEmail;

    private String note;

    @NotNull(message = "ORDER_PAYMENT_METHOD_REQUIRED")
    private PaymentMethod paymentMethod;
}
