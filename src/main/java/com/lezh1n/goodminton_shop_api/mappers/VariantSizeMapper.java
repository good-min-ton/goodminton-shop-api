package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.VariantSizeResponse;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VariantSizeMapper {

    private final SizeMapper sizeMapper;

    public VariantSizeResponse toVariantSizeResponse(VariantSize variantSize) {
        return VariantSizeResponse.builder()
                .variantSizeId(variantSize.getVariantSizeId())
                .size(sizeMapper.toSizeResponse(variantSize.getSize()))
                .price(variantSize.getPrice())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
