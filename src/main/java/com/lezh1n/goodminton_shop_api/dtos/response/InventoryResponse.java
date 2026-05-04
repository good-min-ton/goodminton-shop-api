package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

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
public class InventoryResponse {
    private Integer inventoryId;
    private Integer storeId;
    private String storeName;
    private Integer variantId;
    private String skuCode;
    private Integer productId;
    private String productName;
    private ColorResponse color;
    private SizeResponse size;
    private Integer quantity;
    private LocalDateTime updatedAt;
}
