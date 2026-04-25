package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductListItemResponse {
    private Integer productId;
    private String name;
    private String slug;
    private String thumbnailUrl;
    private BigDecimal minPrice;
    private BigDecimal minSalePrice;
}
