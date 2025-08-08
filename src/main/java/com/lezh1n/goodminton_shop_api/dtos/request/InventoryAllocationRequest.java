package com.lezh1n.goodminton_shop_api.dtos.request;

import lombok.Getter;

@Getter
public class InventoryAllocationRequest {
    private Integer inventoryId;
    private Integer quantity;
}
