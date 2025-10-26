package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.entities.Brand;
import com.lezh1n.goodminton_shop_api.entities.Category;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.BrandRepository;
import com.lezh1n.goodminton_shop_api.repositories.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductMapper {

	private final CategoryRepository categoryRepository;
	private final BrandRepository brandRepository;
	private final CategoryMapper categoryMapper;
	private final BrandMapper brandMapper;

	public Product toProduct(ProductRequest request) {

		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

		Brand brand = brandRepository.findById(request.getBrandId())
				.orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

		return Product.builder()
				.category(category)
				.brand(brand)
				.name(request.getName())
				.description(request.getDescription())
				.createAt(LocalDateTime.now())
				.build();
	}

	public ProductResponse toProductResponse(Product product) {

		return ProductResponse.builder()
				.productId(product.getProductId())
				.category(categoryMapper.toCategoryResponse(product.getCategory()))
				.brand(brandMapper.toBrandResponse(product.getBrand()))
				.name(product.getName())
				.description(product.getDescription())
				.thumbnailUrl(product.getThumbnailUrl())
				.createAt(product.getCreateAt())
				.build();
	}

	public void updateProduct(Product product, ProductRequest request) {
		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

		Brand brand = brandRepository.findById(request.getBrandId())
				.orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

		product.setCategory(category);
		product.setBrand(brand);
		product.setName(request.getName());
		product.setDescription(request.getDescription());
	}
}
