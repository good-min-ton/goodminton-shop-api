package com.lezh1n.goodminton_shop_api.dtos.response;

import java.util.List;

public record ProductPricingResponse(
        Integer productId,
        String productName,
        List<VariantPricingResponse> variants) {
}
