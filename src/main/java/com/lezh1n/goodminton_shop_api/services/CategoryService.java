package com.lezh1n.goodminton_shop_api.services;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateCategoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
}
