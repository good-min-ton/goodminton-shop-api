package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.request.CategoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request, MultipartFile thumbnail);

    CategoryResponse getCategoryById(Integer categoryId);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(Integer categoryId, CategoryRequest request, MultipartFile thumbnail);

    void deleteCategory(Integer categoryId);
}
