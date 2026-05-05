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
public class StoreRevenueResponse {
    private Integer storeId;
    private String storeName;
    private BigDecimal revenue;
    private Long orderCount;
}
