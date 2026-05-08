package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.services.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{productId}/recommendations")
    public ApiResponse<List<ProductListItemResponse>> getRecommendations(@PathVariable Integer productId) {
        return ApiResponse.<List<ProductListItemResponse>>builder()
                .result(recommendationService.getRecommendations(productId))
                .build();
    }
}
