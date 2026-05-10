package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
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
    private final ProductMapper productMapper;
    private final ResourceService resourceService;

    @Override
    public Page<ProductListItemResponse> search(String query, int page, int size) {
        String sanitized = sanitize(query);
        int pageNumber = Math.max(0, page);
        int pageSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Page<Product> hits = productRepository.searchProducts(sanitized, pageable);
        if (hits.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, hits.getTotalElements());
        }

        List<Product> withVariants = loadOrderedWithVariants(hits.getContent());
        List<ProductListItemResponse> items = withVariants.stream()
                .map(this::toListItem)
                .toList();
        return new PageImpl<>(items, pageable, hits.getTotalElements());
    }

    @Override
    public List<ProductListItemResponse> suggest(String prefix) {
        String sanitized = sanitize(prefix);
        if (sanitized.length() < MIN_QUERY_LENGTH) {
            return List.of();
        }

        List<Product> hits = productRepository.suggestProducts(sanitized, SUGGEST_LIMIT);
        if (hits.isEmpty()) {
            return List.of();
        }

        return loadOrderedWithVariants(hits).stream()
                .map(this::toListItem)
                .toList();
    }

    private List<Product> loadOrderedWithVariants(List<Product> source) {
        List<Integer> ids = source.stream().map(Product::getId).toList();
        Map<Integer, Product> byId = new LinkedHashMap<>();
        productRepository.findAllByIdInWithVariants(ids).forEach(p -> byId.put(p.getId(), p));
        return ids.stream().map(byId::get).filter(p -> p != null).toList();
    }

    private ProductListItemResponse toListItem(Product product) {
        String thumbnailUrl = resourceService
                .findSingle(ResourceOwner.PRODUCT_THUMBNAIL, product.getId())
                .map(r -> r.getUrl())
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
