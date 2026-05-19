package com.lezh1n.goodminton_shop_api.dtos.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ProductVariantRequest {

    // Present when updating an existing variant; null when creating a new one.
    private Integer id;

    private Integer colorId;

    private Integer sizeId;

    @NotBlank(message = "VARIANT_SKU_BLANK")
    private String skuCode;

    @NotNull(message = "VARIANT_PRICE_BLANK")
    @Positive(message = "VARIANT_PRICE_MUST_BE_POSITIVE")
    private BigDecimal price;

    @Positive(message = "VARIANT_PRICE_MUST_BE_POSITIVE")
    private BigDecimal salePrice;
}
