package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.BrandRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.BrandResponse;
import com.lezh1n.goodminton_shop_api.services.BrandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ApiResponse<BrandResponse> createBrand(@RequestBody BrandRequest request) {
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.createBrand(request))
                .build();
    }

    @GetMapping("/{brandId}")
    public ApiResponse<BrandResponse> getBrandById(@PathVariable Integer brandId) {
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.getBrandById(brandId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<BrandResponse>> getAllBrands() {
        return ApiResponse.<List<BrandResponse>>builder()
                .result(brandService.getAllBrands())
                .build();
    }

    @PutMapping("/{brandId}")
    public ApiResponse<BrandResponse> updateBrand(@PathVariable Integer brandId, @RequestBody BrandRequest request) {
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.updateBrand(brandId, request))
                .build();
    }
}
