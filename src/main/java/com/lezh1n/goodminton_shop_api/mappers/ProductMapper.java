package com.lezh1n.goodminton_shop_api.mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.entities.Brand;
import com.lezh1n.goodminton_shop_api.entities.Category;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.BrandRepository;
import com.lezh1n.goodminton_shop_api.repositories.CategoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;

    public Product toProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        Product related = resolveRelatedProduct(request.getRelatedProductId());

        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .category(category)
                .brand(brand)
                .relatedProduct(related)
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .isVisible(request.getIsVisible() == null || request.getIsVisible())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public ProductResponse toProductResponse(Product product, ResourceResponse thumbnail) {
        return ProductResponse.builder()
                .id(product.getId())
                .category(categoryMapper.toCategoryResponse(product.getCategory()))
                .brand(brandMapper.toBrandResponse(product.getBrand()))
                .relatedProductId(product.getRelatedProduct() == null ? null : product.getRelatedProduct().getId())
                .name(product.getName())
                .description(product.getDescription())
                .slug(product.getSlug())
                .isVisible(product.getIsVisible())
                .thumbnail(thumbnail)
                .createdAt(product.getCreatedAt())
                .build();
    }

    public void updateProduct(Product product, ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        product.setCategory(category);
        product.setBrand(brand);
        product.setRelatedProduct(resolveRelatedProduct(request.getRelatedProductId()));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        if (request.getIsVisible() != null) {
            product.setIsVisible(request.getIsVisible());
        }
        product.setUpdatedAt(LocalDateTime.now());
    }

    public ProductListItemResponse toListItemResponse(Product product, String thumbnailUrl) {
        BigDecimal minPrice = product.getVariants().stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        BigDecimal minSalePrice = product.getVariants().stream()
                .map(ProductVariant::getSalePrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
        return ProductListItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .thumbnailUrl(thumbnailUrl)
                .minPrice(minPrice)
                .minSalePrice(minSalePrice)
                .build();
    }

    private Product resolveRelatedProduct(Integer relatedId) {
        if (relatedId == null) {
            return null;
        }
        Product related = productRepository.findById(relatedId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        // Only allow linking to a root product (related_product_id IS NULL).
        if (related.getRelatedProduct() != null) {
            throw new AppException(ErrorCode.PRODUCT_RELATED_MUST_BE_ROOT);
        }
        return related;
    }
}
