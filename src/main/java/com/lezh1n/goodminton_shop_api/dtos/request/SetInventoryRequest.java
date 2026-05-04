package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
public class SetInventoryRequest {

    @NotNull(message = "INVENTORY_STORE_ID_REQUIRED")
    private Integer storeId;

    @NotNull(message = "INVENTORY_VARIANT_ID_REQUIRED")
    private Integer variantId;

    @NotNull(message = "INVENTORY_QUANTITY_REQUIRED")
    @PositiveOrZero(message = "INVENTORY_QUANTITY_NEGATIVE")
    private Integer quantity;
}
