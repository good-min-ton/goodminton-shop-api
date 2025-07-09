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
import com.lezh1n.goodminton_shop_api.dtos.request.SizeRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.SizeResponse;
import com.lezh1n.goodminton_shop_api.services.SizeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @PostMapping
    public ApiResponse<SizeResponse> createSize(SizeRequest request) {
        return ApiResponse.<SizeResponse>builder()
                .result(sizeService.createSize(request))
                .build();
    }

    @GetMapping("/{sizeId}")
    public ApiResponse<SizeResponse> getSizeById(@PathVariable Integer sizeId) {
        return ApiResponse.<SizeResponse>builder()
                .result(sizeService.getSizeById(sizeId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<SizeResponse>> getAllSizes() {
        return ApiResponse.<List<SizeResponse>>builder()
                .result(sizeService.getAllSizes())
                .build();
    }

    @PutMapping("/{sizeId}")
    public ApiResponse<SizeResponse> updateSize(@PathVariable Integer sizeId, @RequestBody SizeRequest request) {
        return ApiResponse.<SizeResponse>builder()
                .result(sizeService.updateSize(sizeId, request))
                .build();
    }
}
