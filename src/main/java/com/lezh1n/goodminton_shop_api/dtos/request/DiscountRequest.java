package com.lezh1n.goodminton_shop_api.dtos.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DiscountRequest {
    @NotNull(message = "DISCOUNT_SALE_PRICE_NULL")
    private BigDecimal salePrice;

    @NotNull(message = "DISCOUNT_START_TIME_NULL")
    private LocalDateTime startTime;

    @NotNull(message = "DISCOUNT_END_TIME_NULL")
    private LocalDateTime endTime;
}
