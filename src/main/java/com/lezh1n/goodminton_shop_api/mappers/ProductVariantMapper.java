package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;
import com.lezh1n.goodminton_shop_api.entities.Color;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Size;
import com.lezh1n.goodminton_shop_api.entities.Version;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.ColorRepository;
import com.lezh1n.goodminton_shop_api.repositories.SizeRepository;
import com.lezh1n.goodminton_shop_api.repositories.VersionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductVariantMapper {

    private final VersionRepository versionRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductMapper productMapper;
    private final VersionMapper versionMapper;
    private final ColorMapper colorMapper;
    private final SizeMapper sizeMapper;

    public ProductVariant toProductVariant(Product product, ProductVariantRequest request) {
        Version version = versionRepository.findById(request.getVersionId())
                .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));

        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        return ProductVariant.builder()
                .product(product)
                .version(version)
                .color(color)
                .size(size)
                .price(request.getPrice())
                .build();
    }

    public ProductVariantResponse toProductVariantResponse(ProductVariant productVariant) {

        return ProductVariantResponse.builder()
                .variantId(productVariant.getVariantId())
                .product(productMapper.toProductResponse(productVariant.getProduct()))
                .version(versionMapper.toVersionResponse(productVariant.getVersion()))
                .color(colorMapper.toColorResponse(productVariant.getColor()))
                .size(sizeMapper.toSizeResponse(productVariant.getSize()))
                .price(productVariant.getPrice())
                .build();
    }

    public void updateProductVariant(ProductVariant productVariant, ProductVariantRequest request) {
        Version version = versionRepository.findById(request.getVersionId())
                .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));

        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        productVariant.setVersion(version);
        productVariant.setColor(color);
        productVariant.setSize(size);
        productVariant.setPrice(request.getPrice());
    }

}
