package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateCategoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Category;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.CategoryMapper;
import com.lezh1n.goodminton_shop_api.repositories.CategoryRepository;
import com.lezh1n.goodminton_shop_api.services.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        Category category = categoryMapper.toCategory(request);

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getCategoryById(Integer categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(categoryMapper::toCategoryResponse).toList();
    }

}
