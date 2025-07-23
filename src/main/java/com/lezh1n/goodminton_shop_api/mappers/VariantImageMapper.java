package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.VariantImageResponse;
import com.lezh1n.goodminton_shop_api.entities.VariantImage;

@Component
public class VariantImageMapper {

    public VariantImageResponse toVariantImageResponse(VariantImage variantImage) {
        return VariantImageResponse.builder()
                .imageId(variantImage.getImageId())
                .publicId(variantImage.getPublicId())
                .imageUrl(variantImage.getImageUrl())
                .sortOrder(variantImage.getSortOrder())
                .createAt(variantImage.getCreateAt())
                .build();
    }
}
