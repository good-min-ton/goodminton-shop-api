package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.SetInventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Inventory;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.InventoryMapper;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.InventoryService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryMapper inventoryMapper;
    private final CurrentAccountProvider currentAccountProvider;

    @Override
    public void deduct(Integer storeId, Integer variantId, int quantity) {
        if (quantity <= 0) {
            throw new AppException(ErrorCode.INVENTORY_QUANTITY_REQUIRED);
        }
        int updated = inventoryRepository.decrementIfAvailable(storeId, variantId, quantity, LocalDateTime.now());
        if (updated == 0) {
            // Distinguish "no inventory row" vs "insufficient stock".
            inventoryRepository.findByStore_IdAndVariant_Id(storeId, variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_VARIANT_NOT_FOUND));
            throw new AppException(ErrorCode.ORDER_INVENTORY_INSUFFICIENT);
        }
    }

    @Override
    public void restock(Integer storeId, Integer variantId, int quantity) {
        if (quantity <= 0) {
            return;
        }
        int updated = inventoryRepository.increment(storeId, variantId, quantity, LocalDateTime.now());
        if (updated == 0) {
            throw new AppException(ErrorCode.INVENTORY_VARIANT_NOT_FOUND);
        }
    }

    @Override
    public Store findCentralStore() {
        return storeRepository.findByIsCentralTrue()
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NO_CENTRAL));
    }

    @Override
    public InventoryResponse setQuantity(SetInventoryRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        Inventory inventory = inventoryRepository
                .findByStore_IdAndVariant_Id(store.getId(), variant.getId())
                .orElseGet(() -> Inventory.builder()
                        .store(store)
                        .variant(variant)
                        .build());

        inventory.setQuantity(request.getQuantity());
        inventory.setUpdatedAt(LocalDateTime.now());
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponse getById(Integer inventoryId) {
        return inventoryMapper.toInventoryResponse(inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND)));
    }

    @Override
    public Page<InventoryResponse> listByStore(Integer storeId, Pageable pageable) {
        if (!storeRepository.existsById(storeId)) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        return inventoryRepository.findByStore_Id(storeId, pageable).map(inventoryMapper::toInventoryResponse);
    }

    @Override
    public Page<InventoryResponse> listMyStore(Pageable pageable) {
        Account current = currentAccountProvider.getCurrentAccount();
        Store store = storeRepository.findByAdmin_Id(current.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        return inventoryRepository.findByStore_Id(store.getId(), pageable).map(inventoryMapper::toInventoryResponse);
    }
}
