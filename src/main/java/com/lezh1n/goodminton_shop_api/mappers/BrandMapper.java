package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.BrandRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.BrandResponse;
import com.lezh1n.goodminton_shop_api.entities.Brand;

@Component
public class BrandMapper {
    public Brand toBrand(BrandRequest request) {
        return Brand.builder()
                .name(request.getBrandName())
                .build();
    }

    public BrandResponse toBrandResponse(Brand brand) {
        return BrandResponse.builder()
                .brandId(brand.getId())
                .brandName(brand.getName())
                .build();
    }
}
