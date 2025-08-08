package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.OrderItemResponse;
import com.lezh1n.goodminton_shop_api.entities.OrderItem;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.VariantSizeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    private final VariantSizeRepository variantSizeRepository;
    private final VersionMapper versionMapper;
    private final ColorMapper colorMapper;
    private final SizeMapper sizeMapper;

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        VariantSize variantSize = variantSizeRepository.findById(item.getVariantSize().getVariantSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_SIZE_NOT_FOUND));
        Product product = variantSize.getVariant().getProduct();
        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .variantSizeId(item.getVariantSize().getVariantSizeId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .productId(product.getProductId())
                .productName(product.getName())
                .version(versionMapper.toVersionResponse(variantSize.getVariant().getVersion()))
                .color(colorMapper.toColorResponse(variantSize.getVariant().getColor()))
                .size(sizeMapper.toSizeResponse(variantSize.getSize()))
                .build();
    }
}
