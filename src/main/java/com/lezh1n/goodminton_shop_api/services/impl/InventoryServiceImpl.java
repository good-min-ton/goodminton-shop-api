package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.InventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryByProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryByStoreResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryByVariantResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.VariantSizeInventoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Inventory;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Store;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ColorMapper;
import com.lezh1n.goodminton_shop_api.mappers.InventoryMapper;
import com.lezh1n.goodminton_shop_api.mappers.VersionMapper;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.services.InventoryService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final InventoryMapper inventoryMapper;
    private final VersionMapper versionMapper;
    private final ColorMapper colorMapper;

    @Override
    public InventoryResponse createInventory(InventoryRequest request) {
        if (inventoryRepository.existsByStoreStoreIdAndVariantSizeVariantSizeId(request.getStoreId(),
                request.getVariantSizeId())) {
            throw new AppException(ErrorCode.INVENTORY_STORE_AND_VARIANT_DUPLICATED);
        }
        Inventory inventory = inventoryMapper.toInventory(request);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponse updateInventory(Integer inventoryId, InventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        inventoryMapper.updateInventory(inventory, request);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
    }

    @Override
    public void deleteInventory(Integer inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryByStoreResponse getInventoriesByStore(Integer storeId, int page, int size, String sortBy,
            String sortDir) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Inventory> inventoryPage = inventoryRepository.findByStoreStoreId(storeId, pageable);

        Map<Product, Map<ProductVariant, List<VariantSizeInventoryResponse>>> productMap = new HashMap<>();

        for (Inventory inventory : inventoryPage.getContent()) {
            VariantSize variantSize = inventory.getVariantSize();
            ProductVariant variant = variantSize.getVariant();
            Product product = variant.getProduct();

            VariantSizeInventoryResponse sizeResponse = VariantSizeInventoryResponse.builder()
                    .variantSizeId(variantSize.getVariantSizeId())
                    .price(variantSize.getPrice())
                    .updateAt(inventory.getUpdatedAt())
                    .inventoryId(inventory.getInventoryId())
                    .quantity(inventory.getQuantity())
                    .build();
            productMap.computeIfAbsent(product, k -> new HashMap<>())
                    .computeIfAbsent(variant, k -> new ArrayList<>())
                    .add(sizeResponse);
        }

        List<InventoryByProductResponse> productResponses = productMap.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<InventoryByVariantResponse> variantResponses = entry.getValue().entrySet().stream()
                            .map(variantEntry -> InventoryByVariantResponse.builder()
                                    .variantId(variantEntry.getKey().getVariantId())
                                    .version(versionMapper.toVersionResponse(variantEntry.getKey().getVersion()))
                                    .color(colorMapper.toColorResponse(variantEntry.getKey().getColor()))
                                    .variantSizeInventory(variantEntry.getValue())
                                    .build())
                            .toList();
                    return InventoryByProductResponse.builder()
                            .productId(product.getProductId())
                            .productName(product.getName())
                            .createAt(product.getCreateAt())
                            .variants(variantResponses)
                            .build();
                }).toList();
        return InventoryByStoreResponse.builder()
                .storeId(store.getStoreId())
                .storeName(store.getName())
                .storeAddress(store.getAddress())
                .products(productResponses)
                .page(page)
                .size(size)
                .totalElements(inventoryPage.getTotalElements())
                .totalPages(inventoryPage.getTotalPages())
                .build();
    }

}
