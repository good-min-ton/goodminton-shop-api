package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponse {
    private Integer orderItemId;
    private Integer variantSizeId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Integer productId;
    private String productName;
    private VersionResponse version;
    private ColorResponse color;
    private SizeResponse size;
}
