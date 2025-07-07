package com.lezh1n.goodminton_shop_api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Integer categoryId;
    private String name;
    private String description;
}
