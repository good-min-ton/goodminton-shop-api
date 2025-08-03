package com.lezh1n.goodminton_shop_api.services;

import com.lezh1n.goodminton_shop_api.dtos.request.InventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryByStoreResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;

public interface InventoryService {
    InventoryResponse createInventory(InventoryRequest request);

    InventoryResponse updateInventory(Integer inventoryId, InventoryRequest request);

    void deleteInventory(Integer inventoryId);

    InventoryByStoreResponse getInventoriesByStore(Integer storeId, int page, int size, String sortBy, String sortDir);
}
