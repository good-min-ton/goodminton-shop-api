package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.request.DiscountRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.DiscountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductByAttributeResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.VariantImageResponse;

public interface ProductService {
    // Product CRUD
    ProductResponse createProduct(ProductRequest request, MultipartFile thumbnail);

    ProductResponse getProductById(Integer id);

    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    ProductResponse updateProduct(Integer productId, ProductRequest request, MultipartFile thumbnail);

    void deleteProduct(Integer productId);

    // Specific variant
    ProductByAttributeResponse getProductByAttributes(Integer productId, Integer versionId, Integer colorId,
            Integer sizeId);

    // Variant image
    VariantImageResponse uploadVariantImage(Integer variantId, MultipartFile file);

    void deleteVariantImage(Integer imageId);

    // Product discount
    DiscountResponse createDiscount(Integer variantSizeId, DiscountRequest request);

    // Product comments
    ReviewResponse createReview(Integer productId, ReviewRequest request);

    Page<ReviewResponse> getReviewsOfProduct(Integer productId, int page, int size, String sortBy, String sortDir);
}
