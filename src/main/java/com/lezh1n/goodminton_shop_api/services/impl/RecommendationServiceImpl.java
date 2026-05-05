package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.configurations.CacheConfig;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.repositories.OrderItemRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.services.RecommendationService;
import com.lezh1n.goodminton_shop_api.services.ResourceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final int TARGET_SIZE = 8;
    private static final int CATEGORY_BRAND_LIMIT = 5;
    private static final int BESTSELLER_DAYS = 30;

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;
    private final ResourceService resourceService;

    @Override
    @Cacheable(value = CacheConfig.RECOMMENDATIONS_CACHE, key = "#productId")
    public List<ProductListItemResponse> getRecommendations(Integer productId) {
        Product current = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Set<Integer> excluded = buildExcludedSet(current);

        List<Product> picked = new ArrayList<>(TARGET_SIZE);

        // Source 1: same category + brand
        List<Product> similar = productRepository.findSimilar(
                current.getCategory().getId(),
                current.getBrand().getId(),
                excluded,
                PageRequest.of(0, CATEGORY_BRAND_LIMIT));
        addAll(picked, similar, excluded);

        // Source 2: best sellers in last 30 days
        int remaining = TARGET_SIZE - picked.size();
        if (remaining > 0) {
            List<Integer> bestSellerIds = orderItemRepository.findBestSellerProductIds(
                    OrderStatus.COMPLETED,
                    LocalDateTime.now().minusDays(BESTSELLER_DAYS),
                    excluded,
                    PageRequest.of(0, remaining));
            addAll(picked, loadOrdered(bestSellerIds), excluded);
        }

        // Source 3: variants currently on sale
        remaining = TARGET_SIZE - picked.size();
        if (remaining > 0) {
            List<Product> onSale = productRepository.findOnSale(excluded, PageRequest.of(0, remaining));
            addAll(picked, onSale, excluded);
        }

        return picked.stream().map(this::toListItem).toList();
    }

    private Set<Integer> buildExcludedSet(Product current) {
        Set<Integer> excluded = new LinkedHashSet<>();
        excluded.add(current.getId());

        // Resolve root then collect all siblings, so we exclude the whole related family.
        Integer rootId = current.getRelatedProduct() != null
                ? current.getRelatedProduct().getId()
                : current.getId();
        excluded.add(rootId);
        excluded.addAll(productRepository.findIdsByRelatedProduct_Id(rootId));
        return excluded;
    }

    private void addAll(List<Product> target, List<Product> source, Set<Integer> excluded) {
        for (Product p : source) {
            if (excluded.add(p.getId())) {
                target.add(p);
            }
        }
    }

    private List<Product> loadOrdered(List<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Integer, Product> byId = new LinkedHashMap<>();
        productRepository.findAllByIdInWithVariants(ids).forEach(p -> byId.put(p.getId(), p));
        List<Product> ordered = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            Product p = byId.get(id);
            if (p != null) {
                ordered.add(p);
            }
        }
        return ordered;
    }

    private ProductListItemResponse toListItem(Product product) {
        String thumbnailUrl = resourceService
                .findSingle(ResourceOwner.PRODUCT_THUMBNAIL, product.getId())
                .map(r -> r.getUrl())
                .orElse(null);
        return productMapper.toListItemResponse(product, thumbnailUrl);
    }
}
