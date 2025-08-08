package com.lezh1n.goodminton_shop_api.dtos.request;

import lombok.Getter;

@Getter
public class OrderItemRequest {
    private Integer variantSizeId;
    private Integer quantity;
}
