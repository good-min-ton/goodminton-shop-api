package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.AccountMapper;
import com.lezh1n.goodminton_shop_api.mappers.StoreMapper;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.services.StoreService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreServiceImpl implements StoreService {

    private final AccountRepository accountRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final AccountMapper accountMapper;
    private final StoreMapper storeMapper;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public StoreResponse createStore(CreateStoreRequest request) {
        if (request.getAdminId() == null) {
            throw new AppException(ErrorCode.STORE_ADMIN_ID_REQUIRED);
        }

        Account account = accountRepository.findById(request.getAdminId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        checkValidAdminAccount(account.getId());

        Store store = storeMapper.toStore(request);
        store.setAdmin(account);

        // Only one central store allowed — demote current central before promoting new one.
        if (store.isCentral()) {
            demoteCurrentCentral();
        }

        return storeMapper.toStoreResponse(storeRepository.save(store));
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

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<AccountResponse> getAllAdminsAvalable() {
        return accountRepository.findAdminsNotAssigned().stream().map(accountMapper::toAccountResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public StoreResponse updateStoreAdmin(Integer storeId, Integer adminId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        Account admin = accountRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        checkValidAdminAccount(adminId);
        store.setAdmin(admin);

        return storeMapper.toStoreResponse(storeRepository.save(store));
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public StoreResponse setCentral(Integer storeId) {
        Store target = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (!target.isCentral()) {
            demoteCurrentCentral();
            target.setCentral(true);
            storeRepository.save(target);
        }
        return storeMapper.toStoreResponse(target);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteStore(Integer storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        if (inventoryRepository.existsByStore_Id(storeId)) {
            throw new AppException(ErrorCode.STORE_INVENTORY_EXISTED);
        }

        storeRepository.delete(store);
    }

    private void checkValidAdminAccount(Integer adminId) {
        if (!accountRepository.isStoreAdminAccount(adminId)) {
            throw new AppException(ErrorCode.STORE_ASSIGN_NON_ADMIN_ACCOUNT);
        }

        if (storeRepository.isAdminAssigned(adminId)) {
            throw new AppException(ErrorCode.STORE_ADMIN_ASSIGNED);
        }
    }

    private void demoteCurrentCentral() {
        storeRepository.findByIsCentralTrue().ifPresent(s -> {
            s.setCentral(false);
            storeRepository.save(s);
        });
    }
}
