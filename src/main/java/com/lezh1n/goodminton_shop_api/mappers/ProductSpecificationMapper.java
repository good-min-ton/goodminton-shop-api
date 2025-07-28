package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductSpecificationRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductSpecificationResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductSpecification;

@Component
public class ProductSpecificationMapper {
    public ProductSpecification toProductSpecification(Product product, ProductSpecificationRequest request) {
        return ProductSpecification.builder()
                .product(product)
                .name(request.getName())
                .value(request.getValue())
                .build();
    }

    public ProductSpecificationResponse toSpecificationResponse(ProductSpecification productSpecification) {
        return ProductSpecificationResponse.builder()
                .specId(productSpecification.getSpecId())
                .name(productSpecification.getName())
                .value(productSpecification.getValue())
                .build();
    }
}
