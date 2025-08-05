package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import lombok.Getter;

@Getter
public class OrderItemAllocationRequest {
    private Integer orderItemId;
    private List<InventoryAllocationRequest> inventoryAllocations;
}
