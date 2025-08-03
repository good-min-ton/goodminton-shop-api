package com.lezh1n.goodminton_shop_api.dtos.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryByStoreResponse {
    private Integer storeId;
    private String storeName;
    private String storeAddress;
    private List<InventoryByProductResponse> products;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
