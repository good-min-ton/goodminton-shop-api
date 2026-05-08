package com.lezh1n.goodminton_shop_api.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
}
