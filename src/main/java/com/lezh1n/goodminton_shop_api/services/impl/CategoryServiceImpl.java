package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.request.CategoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.entities.Category;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.CategoryMapper;
import com.lezh1n.goodminton_shop_api.repositories.CategoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.services.CategoryService;
import com.lezh1n.goodminton_shop_api.services.ResourceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final ResourceService resourceService;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile thumbnail) {
        Category saved = categoryRepository.save(categoryMapper.toCategory(request));

        if (thumbnail != null && !thumbnail.isEmpty()) {
            resourceService.upload(ResourceOwner.CATEGORY_THUMBNAIL, saved.getId(), thumbnail);
        }
        return buildResponse(saved);
    }

    @Override
    public CategoryResponse getCategoryById(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return buildResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::buildResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public CategoryResponse updateCategory(Integer categoryId, CategoryRequest request, MultipartFile thumbnail) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        Category saved = categoryRepository.save(category);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            resourceService.replaceSingle(ResourceOwner.CATEGORY_THUMBNAIL, saved.getId(), thumbnail);
        }
        return buildResponse(saved);
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteCategory(Integer categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (productRepository.existsByCategory_Id(categoryId)) {
            throw new AppException(ErrorCode.CATEGORY_PRODUCT_EXISTED);
        }

        resourceService.deleteByOwner(ResourceOwner.CATEGORY_THUMBNAIL, categoryId);
        categoryRepository.delete(category);
    }

    private CategoryResponse buildResponse(Category category) {
        ResourceResponse thumbnail = resourceService
                .findSingle(ResourceOwner.CATEGORY_THUMBNAIL, category.getId())
                .orElse(null);
        return categoryMapper.toCategoryResponse(category, thumbnail);
    }
}
