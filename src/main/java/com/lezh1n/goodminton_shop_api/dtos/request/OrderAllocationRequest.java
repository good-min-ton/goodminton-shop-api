package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import lombok.Getter;

@Getter
public class OrderAllocationRequest {
    private Integer orderId;
    private List<OrderItemAllocationRequest> allocations;
}
