package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.request.BrandRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.BrandResponse;

public interface BrandService {
    BrandResponse createBrand(BrandRequest request);

    BrandResponse getBrandById(Integer brandId);

    List<BrandResponse> getAllBrands();

    BrandResponse updateBrand(Integer brandId, BrandRequest request);

    void deleteBrand(Integer brandId);
}
