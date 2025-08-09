package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscountResponse {
    private Integer discountId;
    private Integer variantSizeId;
    private BigDecimal salePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createAt;
}
