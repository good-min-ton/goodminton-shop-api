package com.lezh1n.goodminton_shop_api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandResponse {
    private Integer brandId;
    private String brandName;
}
