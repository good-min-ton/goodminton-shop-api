package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;

public interface SearchService {

    Page<ProductListItemResponse> search(String query, int page, int size);

    List<ProductListItemResponse> suggest(String prefix);
}
