package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lezh1n.goodminton_shop_api.dtos.request.SetInventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Store;

public interface InventoryService {

    /** Deduct stock atomically; fails when the on-hand quantity is insufficient. */
    void deduct(Integer storeId, Integer variantId, int quantity);

    /** Restock when an order is cancelled. */
    void restock(Integer storeId, Integer variantId, int quantity);

    /** Return the central store (is_central = true) used to fulfil ONLINE orders. */
    Store findCentralStore();

    /** Set the absolute quantity; creates a new inventory row if (store, variant) has none. */
    InventoryResponse setQuantity(SetInventoryRequest request);

    InventoryResponse getById(Integer inventoryId);

    Page<InventoryResponse> listByStore(Integer storeId, Pageable pageable);

    /** List inventory of the store the current STORE_ADMIN manages. */
    Page<InventoryResponse> listMyStore(Pageable pageable);
}
