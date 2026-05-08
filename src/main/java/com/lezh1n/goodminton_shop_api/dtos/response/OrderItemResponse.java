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
public class OrderItemResponse {
    private Integer id;
    private Integer variantId;
    private Integer productId;
    private String productName;
    private String skuCode;
    private ColorResponse color;
    private SizeResponse size;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPrice;
}
