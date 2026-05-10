package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.BrandResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.CategoryResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.mappers.AccountMapper;
import com.lezh1n.goodminton_shop_api.mappers.BrandMapper;
import com.lezh1n.goodminton_shop_api.mappers.CategoryMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.mappers.StoreMapper;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.repositories.BrandRepository;
import com.lezh1n.goodminton_shop_api.repositories.CategoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.StoreRepository;
import com.lezh1n.goodminton_shop_api.services.ResourceService;
import com.lezh1n.goodminton_shop_api.services.SearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int SUGGEST_LIMIT = 8;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final AccountRepository accountRepository;
    private final StoreRepository storeRepository;

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final AccountMapper accountMapper;
    private final StoreMapper storeMapper;

    private final ResourceService resourceService;

    @Override
    public Page<ProductListItemResponse> searchProducts(String query, int page, int size) {
        String sanitized = sanitize(query);
        Pageable pageable = pageable(page, size);

        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Page<Product> hits = productRepository.searchProducts(sanitized, pageable);
        if (hits.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, hits.getTotalElements());
        }

        List<Product> withVariants = loadOrderedWithVariants(hits.getContent());
        List<ProductListItemResponse> items = withVariants.stream()
                .map(this::toProductListItem)
                .toList();
        return new PageImpl<>(items, pageable, hits.getTotalElements());
    }

    @Override
    public List<ProductListItemResponse> suggestProducts(String prefix) {
        String sanitized = sanitize(prefix);
        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return List.of();
        }

        String tsquery = toPrefixTsQuery(sanitized);
        if (tsquery.isEmpty()) {
            return List.of();
        }

        List<Product> hits = productRepository.suggestProducts(tsquery, sanitized, SUGGEST_LIMIT);
        if (hits.isEmpty()) {
            return List.of();
        }

        return loadOrderedWithVariants(hits).stream()
                .map(this::toProductListItem)
                .toList();
    }

    private String toPrefixTsQuery(String sanitized) {
        return java.util.Arrays.stream(sanitized.toLowerCase().split("\\s+"))
                .filter(w -> !w.isBlank())
                .map(w -> java.text.Normalizer.normalize(w, java.text.Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                        .replace("đ", "d") + ":*")
                .reduce((a, b) -> a + " & " + b)
                .orElse("");
    }

    @Override
    public Page<CategoryResponse> searchCategories(String query, int page, int size) {
        String sanitized = sanitize(query);
        Pageable pageable = pageable(page, size);

        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        return categoryRepository.searchCategories(sanitized, pageable)
                .map(c -> {
                    ResourceResponse thumb = resourceService
                            .findSingle(ResourceOwner.CATEGORY_THUMBNAIL, c.getId())
                            .orElse(null);
                    return categoryMapper.toCategoryResponse(c, thumb);
                });
    }

    @Override
    public Page<BrandResponse> searchBrands(String query, int page, int size) {
        String sanitized = sanitize(query);
        Pageable pageable = pageable(page, size);

        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        return brandRepository.searchBrands(sanitized, pageable)
                .map(brandMapper::toBrandResponse);
    }

    @Override
    public Page<AccountResponse> searchAccounts(String query, int page, int size) {
        String sanitized = sanitize(query);
        Pageable pageable = pageable(page, size);

        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        return accountRepository.searchAccounts(sanitized, pageable)
                .map(accountMapper::toAccountResponse);
    }

    @Override
    public Page<StoreResponse> searchStores(String query, int page, int size) {
        String sanitized = sanitize(query);
        Pageable pageable = pageable(page, size);

        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        return storeRepository.searchStores(sanitized, pageable)
                .map(storeMapper::toStoreResponse);
    }

    private Pageable pageable(int page, int size) {
        int pageNumber = Math.max(0, page);
        int pageSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        return PageRequest.of(pageNumber, pageSize);
    }

    private List<Product> loadOrderedWithVariants(List<Product> source) {
        List<Integer> ids = source.stream().map(Product::getId).toList();
        Map<Integer, Product> byId = new LinkedHashMap<>();
        productRepository.findAllByIdInWithVariants(ids).forEach(p -> byId.put(p.getId(), p));
        return ids.stream().map(byId::get).filter(p -> p != null).toList();
    }

    private ProductListItemResponse toProductListItem(Product product) {
        String thumbnailUrl = resourceService
                .findSingle(ResourceOwner.PRODUCT_THUMBNAIL, product.getId())
                .map(ResourceResponse::getUrl)
                .orElse(null);
        return productMapper.toListItemResponse(product, thumbnailUrl);
    }

    private String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[^\\p{L}\\p{N}\\s]", "").replaceAll("\\s+", " ");
    }
}
