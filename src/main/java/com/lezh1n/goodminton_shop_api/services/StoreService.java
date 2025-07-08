package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;

public interface StoreService {
    StoreResponse createStore(CreateStoreRequest request);

    StoreResponse getStoreById(Integer id);
    
    List<StoreResponse> getAllStores();

    List<AccountResponse> getAllAdminsAvalable();

    StoreResponse updateStoreAdmin(Integer storeId, Integer adminId);

    void deleteStore(Integer storeId);
}
