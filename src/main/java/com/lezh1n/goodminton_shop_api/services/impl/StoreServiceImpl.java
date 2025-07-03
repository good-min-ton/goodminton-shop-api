package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.StoreMapper;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.services.StoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final AccountRepository accountRepository;
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public StoreResponse createStore(CreateStoreRequest request) {
        Store store = storeMapper.toStore(request);

        Account account = accountRepository.findById(request.getAdminId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        store.setAdmin(account);

        return storeMapper.toStoreResponse(store);
    }

    @Override
    public StoreResponse getStoreById(Integer id) {
        Store store = storeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        return storeMapper.toStoreResponse(store);
    }

    @Override
    public List<StoreResponse> getAllStores() {
        return storeRepository.findAll().stream().map(storeMapper::toStoreResponse).toList();
    }

}
