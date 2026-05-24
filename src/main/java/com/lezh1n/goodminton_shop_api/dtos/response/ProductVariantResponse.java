package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductVariantResponse {
    private Integer id;
    private ColorResponse color;
    private SizeResponse size;
    private String skuCode;
    private BigDecimal price;
    private BigDecimal salePrice;
}
