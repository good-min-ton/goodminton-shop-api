package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductSpecificationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductSpecificationResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;

public interface ProductService {
    // Product CRUD
    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Integer id);

    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    ProductResponse updateProduct(Integer productId, ProductRequest request);

    // Product Specification CRUD
    ProductSpecificationResponse addSpecificationToProduct(Integer productId, ProductSpecificationRequest request);

    // Product Variant CRUD
    ProductVariantResponse addVariantToProduct(Integer productId, ProductVariantRequest request);
}
