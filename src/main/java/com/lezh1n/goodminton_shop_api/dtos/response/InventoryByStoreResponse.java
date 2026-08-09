package com.lezh1n.goodminton_shop_api.dtos.response;

/**
 * Stock for one variant at one store.
 *
 * <p>{@code isCentral} marks the store an ONLINE order is fulfilled from
 * ({@code OrderServiceImpl.createOnlineOrder} deducts from
 * {@code InventoryService.findCentralStore()}), so it is the only quantity that
 * decides whether an order can be placed. Stock at the other stores is walk-in
 * only. It rides on this row rather than a separate endpoint so a caller cannot
 * pair fresh quantities with a stale idea of which store is central.
 *
 * <p>Jackson takes a record's property name from the component itself, so this
 * serialises as {@code "isCentral"}: the same name the store request DTOs accept
 * and the admin UI already reads.
 */
public record InventoryByStoreResponse(
        Integer storeId,
        String storeName,
        boolean isCentral,
        Integer quantity) {
}
