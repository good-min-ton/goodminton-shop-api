package com.lezh1n.goodminton_shop_api.dtos.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductVariantResponse {
    private Integer variantId;
    private VersionResponse version;
    private ColorResponse color;
    private List<VariantSizeResponse> sizes;
    private List<VariantImageResponse> images;
}
