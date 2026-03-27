package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.DiscountRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.DiscountResponse;
import com.lezh1n.goodminton_shop_api.entities.ProductDiscount;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;

@Component
public class DiscountMapper {
    public ProductDiscount toProductDiscount(VariantSize variantSize, DiscountRequest request) {
        return ProductDiscount.builder()
                .variantSize(variantSize)
                .salePrice(request.getSalePrice())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createAt(LocalDateTime.now())
                .build();
    }

    public DiscountResponse toDiscountResponse(ProductDiscount discount) {
        return DiscountResponse.builder()
                .discountId(discount.getId())
                .variantSizeId(discount.getVariantSize().getId())
                .salePrice(discount.getSalePrice())
                .startTime(discount.getStartTime())
                .endTime(discount.getEndTime())
                .createAt(discount.getCreateAt())
                .build();
    }
}
