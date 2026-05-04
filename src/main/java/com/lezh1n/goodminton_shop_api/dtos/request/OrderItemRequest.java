package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class OrderItemRequest {

    @NotNull(message = "ORDER_ITEM_VARIANT_REQUIRED")
    private Integer variantId;

    @NotNull(message = "ORDER_ITEM_QUANTITY_REQUIRED")
    @Positive(message = "ORDER_ITEM_QUANTITY_INVALID")
    private Integer quantity;
}
