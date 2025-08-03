package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryByProductResponse {
    private Integer productId;
    private String productName;
    private LocalDateTime createAt;
    private List<InventoryByVariantResponse> variants;
}
