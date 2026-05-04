package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lezh1n.goodminton_shop_api.dtos.request.SetInventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Store;

public interface InventoryService {

    /** Trừ tồn kho — fail nếu không đủ. Atomic. */
    void deduct(Integer storeId, Integer variantId, int quantity);

    /** Hoàn tồn kho khi cancel order. */
    void restock(Integer storeId, Integer variantId, int quantity);

    /** Tìm kho trung tâm (is_central = true) — dùng cho fulfill đơn ONLINE. */
    Store findCentralStore();

    /** Set quantity tuyệt đối. Tạo mới nếu chưa có inventory cho (store, variant). */
    InventoryResponse setQuantity(SetInventoryRequest request);

    InventoryResponse getById(Integer inventoryId);

    Page<InventoryResponse> listByStore(Integer storeId, Pageable pageable);

    /** Lấy inventory của store mà current user (STORE_ADMIN) đang quản lý. */
    Page<InventoryResponse> listMyStore(Pageable pageable);
}
