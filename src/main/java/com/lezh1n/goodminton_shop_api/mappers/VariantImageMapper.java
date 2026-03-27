package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.VariantImageRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.VariantImageResponse;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.VariantImage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VariantImageMapper {

    public VariantImage toVariantImage(ProductVariant variant, VariantImageRequest request) {
        return VariantImage.builder()
                .variant(variant)
                .publicId(request.getPublicId())
                .imageUrl(request.getImageUrl())
                .sortOrder(request.getSortOrder())
                .createAt(LocalDateTime.now())
                .build();
    }

    public VariantImageResponse toVariantImageResponse(VariantImage variantImage) {
        return VariantImageResponse.builder()
                .imageId(variantImage.getId())
                .publicId(variantImage.getPublicId())
                .imageUrl(variantImage.getImageUrl())
                .sortOrder(variantImage.getSortOrder())
                .createAt(variantImage.getCreateAt())
                .build();
    }
}
