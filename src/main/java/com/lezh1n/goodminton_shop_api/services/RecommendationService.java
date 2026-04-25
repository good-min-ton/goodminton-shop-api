package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;

public interface RecommendationService {

    List<ProductListItemResponse> getRecommendations(Integer productId);
}
