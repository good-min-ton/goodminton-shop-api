package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductVariantResponse {
    private Integer variantId;
    private ProductResponse product;
    private VersionResponse version;
    private ColorResponse color;
    private SizeResponse size;
    private BigDecimal price;
}
