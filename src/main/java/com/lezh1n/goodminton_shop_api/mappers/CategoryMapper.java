package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.CategoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;
import com.lezh1n.goodminton_shop_api.entities.Category;

@Component
public class CategoryMapper {
    public Category toCategory(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
