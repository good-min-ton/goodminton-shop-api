package com.lezh1n.goodminton_shop_api.services;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;

public interface ProductService {
    // Product
    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(Integer id);

    

    // Product variant
    ProductVariantResponse createProductVariant(Integer productId, ProductVariantRequest request);

    ProductVariantResponse getProductVariantById(Integer productVariantId);

    ProductVariantResponse updateProductVariant(Integer productVariantId, ProductVariantRequest request);
}
