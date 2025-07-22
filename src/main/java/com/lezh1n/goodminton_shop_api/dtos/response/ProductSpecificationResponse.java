package com.lezh1n.goodminton_shop_api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSpecificationResponse {
    private Integer specId;
    private String name;
    private String value;
}
