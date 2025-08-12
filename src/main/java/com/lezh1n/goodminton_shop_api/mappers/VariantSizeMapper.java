package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.VariantSizeRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.VariantSizeResponse;
import com.lezh1n.goodminton_shop_api.entities.ProductDiscount;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Size;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.ProductDiscountRepository;
import com.lezh1n.goodminton_shop_api.repositories.SizeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VariantSizeMapper {

    private final SizeRepository sizeRepository;
    private final ProductDiscountRepository productDiscountRepository;
    private final SizeMapper sizeMapper;

    public VariantSize toVariantSize(ProductVariant variant, VariantSizeRequest request) {
        Size size = sizeRepository.findById(request.getSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));
        return VariantSize.builder()
                .variant(variant)
                .size(size)
                .price(request.getPrice())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public VariantSizeResponse toVariantSizeResponse(VariantSize variantSize) {
        Optional<ProductDiscount> discount = productDiscountRepository
                .findActiveDiscountByVariantSizeId(variantSize.getVariantSizeId(), LocalDateTime.now());
        return VariantSizeResponse.builder()
                .variantSizeId(variantSize.getVariantSizeId())
                .size(sizeMapper.toSizeResponse(variantSize.getSize()))
                .price(variantSize.getPrice())
                .discountPrice(discount.map(ProductDiscount::getSalePrice).orElse(null))
                .updatedAt(variantSize.getUpdatedAt())
                .build();
    }
}
