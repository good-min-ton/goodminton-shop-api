package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.UpdateStoreRequest;
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
        ensureAssignableAdmin(account.getId(), null);

        Store store = storeMapper.toStore(request);
        store.setAdmin(account);

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
    public StoreResponse updateStore(Integer storeId, UpdateStoreRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        // Admin re-assignment — skip validation if unchanged.
        if (!request.getAdminId().equals(store.getAdmin().getId())) {
            Account newAdmin = accountRepository.findById(request.getAdminId())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
            ensureAssignableAdmin(newAdmin.getId(), storeId);
            store.setAdmin(newAdmin);
        }

        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setContact(request.getContact());
        store.setLongitude(request.getLongitude());
        store.setLatitude(request.getLatitude());

        // Central flag: null = no change.
        Boolean centralFlag = request.getIsCentral();
        if (centralFlag != null && centralFlag.booleanValue() != store.isCentral()) {
            if (centralFlag.booleanValue()) {
                demoteCurrentCentral();
            }
            store.setCentral(centralFlag.booleanValue());
        }

        return storeMapper.toStoreResponse(storeRepository.save(store));
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

    /**
     * Validate role + uniqueness. selfStoreId allows admin to keep current store.
     */
    private void ensureAssignableAdmin(Integer adminId, Integer selfStoreId) {
        if (!accountRepository.isStoreAdminAccount(adminId)) {
            throw new AppException(ErrorCode.STORE_ASSIGN_NON_ADMIN_ACCOUNT);
        }
        boolean assignedElsewhere = storeRepository.findByAdmin_Id(adminId)
                .filter(s -> selfStoreId == null || !s.getId().equals(selfStoreId))
                .isPresent();
        if (assignedElsewhere) {
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
