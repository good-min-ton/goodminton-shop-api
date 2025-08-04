package com.lezh1n.goodminton_shop_api.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CartItemRequest {
    @NotNull(message = "VARIANT_SIZE_ID_BLANK")
    private Integer variantSizeId;

    @NotNull(message = "INVENTORY_QUANTITY_REQUIRED")
    private Integer quantity;
}
