package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.UpdateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;

public interface StoreService {

    StoreResponse createStore(CreateStoreRequest request);

    StoreResponse getStoreById(Integer id);

    List<StoreResponse> getAllStores();

    List<AccountResponse> getAllAdminsAvalable();

    /** Full update — admin, central flag, contact info. isCentral=null means unchanged. */
    StoreResponse updateStore(Integer storeId, UpdateStoreRequest request);

    void deleteStore(Integer storeId);
}
