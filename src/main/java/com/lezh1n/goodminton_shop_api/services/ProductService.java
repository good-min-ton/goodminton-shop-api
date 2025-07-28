package com.lezh1n.goodminton_shop_api.services;

import org.springframework.data.domain.Page;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;

public interface ProductService {
    // Product
    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Integer id);

    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    // Product Variant
}
