package com.lezh1n.goodminton_shop_api.dtos.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class VariantSizeRequest {
    
    @NotNull(message = "VARIANT_SIZE_BLANK")
    private Integer sizeId;

    @NotNull(message = "VARIANT_PRICE_BLANK")
    @DecimalMin(value = "0.0", message = "VARIANT_PRICE_MUST_BE_POSITIVE")
    private BigDecimal price;

    @NotNull(message = "VARIANT_QUANTITY_BLANK")
    private Integer quantity;
}
