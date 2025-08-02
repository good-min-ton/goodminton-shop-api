package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InventoryRequest {

    @NotNull(message = "INVENTORY_STORE_ID_REQUIRED")
    private Integer storeId;

    @NotNull(message = "INVENTORY_VARIANT_SIZE_REQUIRED")
    private Integer variantSizeId;

    @NotNull(message = "INVENTORY_QUANTITY_REQUIRED")
    private Integer quantity;
}