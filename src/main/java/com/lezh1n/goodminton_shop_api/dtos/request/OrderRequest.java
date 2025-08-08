package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import lombok.Getter;

@Getter
public class OrderRequest {
    private List<OrderItemRequest> items;
    private String name;
    private String phone;
    private String address;
    private String email;
    private String note;
}
