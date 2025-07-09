package com.lezh1n.goodminton_shop_api.dtos.response;

import com.lezh1n.goodminton_shop_api.enums.SizeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SizeResponse {
    private Integer sizeId;
    private String name;
    private SizeType type;
}
