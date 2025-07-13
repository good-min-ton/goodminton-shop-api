package com.lezh1n.goodminton_shop_api.dtos.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ProductVariantRequest {

    @NotNull(message = "VARIANT_VERSION_BLANK")
    private Integer versionId;

    @NotNull(message = "VARIANT_COLOR_BLANK")
    private Integer colorId;

    @NotNull(message = "VARIANT_SIZE_BLANK")
    private Integer sizeId;

    @NotNull(message = "VARIANT_PRICE_BLANK")
    private BigDecimal price;
}
