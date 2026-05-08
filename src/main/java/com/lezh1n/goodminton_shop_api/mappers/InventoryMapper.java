package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Inventory;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryMapper {

    private final ColorMapper colorMapper;
    private final SizeMapper sizeMapper;

    public InventoryResponse toInventoryResponse(Inventory inventory) {
        ProductVariant variant = inventory.getVariant();
        return InventoryResponse.builder()
                .id(inventory.getId())
                .storeId(inventory.getStore().getId())
                .storeName(inventory.getStore().getName())
                .variantId(variant.getId())
                .skuCode(variant.getSkuCode())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())
                .color(variant.getColor() == null ? null : colorMapper.toColorResponse(variant.getColor()))
                .size(variant.getSize() == null ? null : sizeMapper.toSizeResponse(variant.getSize()))
                .quantity(inventory.getQuantity())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
