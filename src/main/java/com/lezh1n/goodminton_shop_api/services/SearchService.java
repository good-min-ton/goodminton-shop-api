package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.BrandResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;

public interface SearchService {

    Page<ProductListItemResponse> searchProducts(String query, int page, int size);

    List<ProductListItemResponse> suggestProducts(String prefix);

    Page<CategoryResponse> searchCategories(String query, int page, int size);

    Page<BrandResponse> searchBrands(String query, int page, int size);

    Page<AccountResponse> searchAccounts(String query, int page, int size);

    Page<StoreResponse> searchStores(String query, int page, int size);
}
