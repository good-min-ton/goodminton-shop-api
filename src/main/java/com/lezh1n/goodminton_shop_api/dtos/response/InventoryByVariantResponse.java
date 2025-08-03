package com.lezh1n.goodminton_shop_api.dtos.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryByVariantResponse {
    private Integer variantId;
    private VersionResponse version;
    private ColorResponse color;
    private List<VariantSizeInventoryResponse> variantSizeInventory;
}
