package com.lezh1n.goodminton_shop_api.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezh1n.goodminton_shop_api.enums.SizeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SizeResponse {
    private Integer id;
    private String name;
    private SizeType type;
}
