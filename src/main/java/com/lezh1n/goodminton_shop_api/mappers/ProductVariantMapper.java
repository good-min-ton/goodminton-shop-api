package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;
import com.lezh1n.goodminton_shop_api.entities.Color;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Size;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.ColorRepository;
import com.lezh1n.goodminton_shop_api.repositories.SizeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductVariantMapper {

    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ColorMapper colorMapper;
    private final SizeMapper sizeMapper;

    public ProductVariant toProductVariant(Product product, ProductVariantRequest request) {
        Color color = request.getColorId() == null ? null
                : colorRepository.findById(request.getColorId())
                        .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        Size size = request.getSizeId() == null ? null
                : sizeRepository.findById(request.getSizeId())
                        .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        return ProductVariant.builder()
                .product(product)
                .color(color)
                .size(size)
                .skuCode(request.getSkuCode())
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void applyUpdate(ProductVariant variant, ProductVariantRequest request) {
        Color color = request.getColorId() == null ? null
                : colorRepository.findById(request.getColorId())
                        .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        Size size = request.getSizeId() == null ? null
                : sizeRepository.findById(request.getSizeId())
                        .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        variant.setColor(color);
        variant.setSize(size);
        variant.setSkuCode(request.getSkuCode());
        variant.setPrice(request.getPrice());
        variant.setSalePrice(request.getSalePrice());
        variant.setUpdatedAt(LocalDateTime.now());
    }

    public ProductVariantResponse toProductVariantResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .color(variant.getColor() == null ? null : colorMapper.toColorResponse(variant.getColor()))
                .size(variant.getSize() == null ? null : sizeMapper.toSizeResponse(variant.getSize()))
                .skuCode(variant.getSkuCode())
                .price(variant.getPrice())
                .salePrice(variant.getSalePrice())
                .build();
    }
}
