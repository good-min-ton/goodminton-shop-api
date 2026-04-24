package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.BrandRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.BrandResponse;
import com.lezh1n.goodminton_shop_api.entities.Brand;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.BrandMapper;
import com.lezh1n.goodminton_shop_api.repositories.BrandRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.services.BrandService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final BrandMapper brandMapper;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public BrandResponse createBrand(BrandRequest request) {
        Brand brand = brandMapper.toBrand(request);
        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    public BrandResponse getBrandById(Integer brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toBrandResponse(brand);
    }

    @Override
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream().map(brandMapper::toBrandResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public BrandResponse updateBrand(Integer brandId, BrandRequest request) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brand.setName(request.getBrandName());
        return brandMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    public void deleteBrand(Integer brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        if (productRepository.existsByBrand_Id(brandId)) {
            throw new AppException(ErrorCode.BRAND_PRODUCT_EXISTED);
        }

        brandRepository.delete(brand);
    }
}
