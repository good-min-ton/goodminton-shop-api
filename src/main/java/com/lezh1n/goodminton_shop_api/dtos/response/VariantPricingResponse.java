package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;

public record VariantPricingResponse(
        Integer variantId,
        String colorName,
        String sizeName,
        String skuCode,
        BigDecimal price,
        BigDecimal salePrice) {
}
