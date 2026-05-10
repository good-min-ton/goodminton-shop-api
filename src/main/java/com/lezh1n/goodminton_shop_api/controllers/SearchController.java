package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.services.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ApiResponse<Page<ProductListItemResponse>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ApiResponse.<Page<ProductListItemResponse>>builder()
                .result(searchService.search(q, page, size))
                .build();
    }

    @GetMapping("/suggest")
    public ApiResponse<List<ProductListItemResponse>> suggest(@RequestParam("q") String q) {
        return ApiResponse.<List<ProductListItemResponse>>builder()
                .result(searchService.suggest(q))
                .build();
    }
}
