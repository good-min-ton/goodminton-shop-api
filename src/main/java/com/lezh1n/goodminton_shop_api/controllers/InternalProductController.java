package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dtos.response.ProductForRagResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductRepository productRepository;

    @GetMapping("/{id}")
    public ProductForRagResponse getForRag(@PathVariable Integer id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        List<Map<String, String>> specs = p.getSpecifications().stream()
                .map(s -> Map.of("name", s.getName(), "value", s.getValue()))
                .toList();

        return ProductForRagResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .brand(p.getBrand().getName())
                .category(p.getCategory().getName())
                .specifications(specs)
                .build();
    }
}
