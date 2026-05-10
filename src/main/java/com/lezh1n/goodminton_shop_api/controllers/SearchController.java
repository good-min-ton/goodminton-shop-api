package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.BrandResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;
import com.lezh1n.goodminton_shop_api.services.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/products")
    public ApiResponse<Page<ProductListItemResponse>> searchProducts(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.<Page<ProductListItemResponse>>builder()
                .result(searchService.searchProducts(q, page, size))
                .build();
    }

    @GetMapping("/products/suggest")
    public ApiResponse<List<ProductListItemResponse>> suggestProducts(@RequestParam("q") String q) {
        return ApiResponse.<List<ProductListItemResponse>>builder()
                .result(searchService.suggestProducts(q))
                .build();
    }

    @GetMapping("/categories")
    public ApiResponse<Page<CategoryResponse>> searchCategories(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.<Page<CategoryResponse>>builder()
                .result(searchService.searchCategories(q, page, size))
                .build();
    }

    @GetMapping("/brands")
    public ApiResponse<Page<BrandResponse>> searchBrands(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.<Page<BrandResponse>>builder()
                .result(searchService.searchBrands(q, page, size))
                .build();
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<AccountResponse>> searchAccounts(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.<Page<AccountResponse>>builder()
                .result(searchService.searchAccounts(q, page, size))
                .build();
    }

    @GetMapping("/stores")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<StoreResponse>> searchStores(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.<Page<StoreResponse>>builder()
                .result(searchService.searchStores(q, page, size))
                .build();
    }
}
