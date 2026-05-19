package com.lezh1n.goodminton_shop_api.dtos.response;

public record InventoryByStoreResponse(
        Integer storeId,
        String storeName,
        Integer quantity) {
}
