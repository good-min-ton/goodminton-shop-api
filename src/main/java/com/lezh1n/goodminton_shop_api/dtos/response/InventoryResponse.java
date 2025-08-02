package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse {
    private Integer inventoryId;
    private Integer storeId;
    private VariantSizeResponse variantSize;
    private Integer quantity;
    private LocalDateTime updatedAt;
}
