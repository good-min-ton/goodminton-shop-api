package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.OrderItemResponse;
import com.lezh1n.goodminton_shop_api.entities.OrderItem;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    private final ColorMapper colorMapper;
    private final SizeMapper sizeMapper;

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        ProductVariant variant = item.getVariant();
        return OrderItemResponse.builder()
                .orderItemId(item.getId())
                .variantId(variant.getId())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getName())
                .skuCode(variant.getSkuCode())
                .color(variant.getColor() == null ? null : colorMapper.toColorResponse(variant.getColor()))
                .size(variant.getSize() == null ? null : sizeMapper.toSizeResponse(variant.getSize()))
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountPrice(item.getDiscountPrice())
                .build();
    }
}
