package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.InventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Inventory;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.repositories.VariantSizeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryMapper {

    private final StoreRepository storeRepository;
    private final VariantSizeRepository variantSizeRepository;
    private final StoreMapper storeMapper;
    private final VariantSizeMapper variantSizeMapper;

    public Inventory toInventory(InventoryRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        VariantSize variantSize = variantSizeRepository.findById(request.getVariantSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_SIZE_NOT_FOUND));

        return Inventory.builder()
                .store(store)
                .variantSize(variantSize)
                .quantity(request.getQuantity())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public InventoryResponse toInventoryResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .inventoryId(inventory.getId())
                .store(storeMapper.toStoreResponse(inventory.getStore()))
                .variantSize(variantSizeMapper.toVariantSizeResponse(inventory.getVariantSize()))
                .quantity(inventory.getQuantity())
                .inventoryUpdatedAt(inventory.getUpdatedAt())
                .build();
    }

    public void updateInventory(Inventory inventory, InventoryRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        VariantSize variantSize = variantSizeRepository.findById(request.getVariantSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_SIZE_NOT_FOUND));

        inventory.setStore(store);
        inventory.setVariantSize(variantSize);
        inventory.setQuantity(request.getQuantity());
        inventory.setUpdatedAt(LocalDateTime.now());
    }
}
