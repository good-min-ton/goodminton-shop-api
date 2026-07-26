package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.client.RagCandidate;
import com.lezh1n.goodminton_shop_api.client.RagClient;
import com.lezh1n.goodminton_shop_api.configurations.CacheConfig;
import com.lezh1n.goodminton_shop_api.configurations.RagProperties;
import com.lezh1n.goodminton_shop_api.configurations.RecommendationProperties;
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
    private static final int BESTSELLER_MEMBERSHIP_LIMIT = 1000;

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;
    private final ResourceService resourceService;
    private final RagClient ragClient;
    private final RagProperties ragProperties;
    private final RecommendationProperties recommendationProperties;

    @Override
    @Cacheable(value = CacheConfig.RECOMMENDATIONS_CACHE, key = "#productId")
    public List<ProductListItemResponse> getRecommendations(Integer productId) {
        Product current = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Set<Integer> excluded = buildExcludedSet(current);

        List<Product> picked = new ArrayList<>(TARGET_SIZE);

        // STAGE 1 + 2: semantic retrieve -> business re-rank (semantic-dominant).
        addAll(picked, semanticRerank(productId, excluded), excluded);

        // STAGE 5: FILL to TARGET_SIZE with the existing rule-based pipeline.
        fillRuleBased(current, picked, excluded);

        return picked.stream().map(this::toListItem).toList();
    }

    /**
     * STAGE 1 (retrieve) + STAGE 2 (re-rank). Returns at most TARGET_SIZE products
     * ordered by (similarity + business boosts) DESC. Empty when RAG is unavailable,
     * the product is not indexed, or no candidate survives exclusion/visibility.
     */
    private List<Product> semanticRerank(Integer productId, Set<Integer> excluded) {
        List<RagCandidate> candidates = ragClient.similar(productId, ragProperties.getRetrieveK());
        if (candidates.isEmpty()) {
            return List.of();
        }

        // Preserve semantic order; drop self/family; keep the first similarity per id.
        Map<Integer, Double> similarityById = new LinkedHashMap<>();
        for (RagCandidate c : candidates) {
            if (!excluded.contains(c.productId())) {
                similarityById.putIfAbsent(c.productId(), c.similarity());
            }
        }
        if (similarityById.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = new ArrayList<>(similarityById.keySet());

        // Load ONLY visible products (drops hidden) with variants for the mapper.
        Map<Integer, Product> byId = new LinkedHashMap<>();
        productRepository.findVisibleByIdInWithVariants(ids).forEach(p -> byId.put(p.getId(), p));

        // Business-signal membership sets over the candidate ids.
        Set<Integer> onSaleIds = new HashSet<>(productRepository.findIdsOnSaleIn(ids));
        Set<Integer> bestSellerIds = new HashSet<>(orderItemRepository.findBestSellerProductIds(
                OrderStatus.COMPLETED,
                LocalDateTime.now().minusDays(BESTSELLER_DAYS),
                excluded,
                PageRequest.of(0, BESTSELLER_MEMBERSHIP_LIMIT)));

        record Scored(Product product, double score) {
        }
        List<Scored> scored = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            Product p = byId.get(id);
            if (p == null) {
                continue; // not visible / not found
            }
            double score = similarityById.get(id)
                    + (bestSellerIds.contains(id) ? recommendationProperties.getBoostBestseller() : 0.0)
                    + (onSaleIds.contains(id) ? recommendationProperties.getBoostSale() : 0.0);
            scored.add(new Scored(p, score));
        }
        // Stable sort by score DESC => equal scores keep semantic (insertion) order.
        scored.sort(Comparator.comparingDouble(Scored::score).reversed());

        List<Product> result = new ArrayList<>(Math.min(scored.size(), TARGET_SIZE));
        for (Scored s : scored) {
            if (result.size() == TARGET_SIZE) {
                break;
            }
            result.add(s.product());
        }
        return result;
    }

    /** STAGE 5 fill: unchanged rule-based sources, excluding already-picked ids. */
    private void fillRuleBased(Product current, List<Product> picked, Set<Integer> excluded) {
        int remaining = TARGET_SIZE - picked.size();
        if (remaining > 0) {
            List<Product> similar = productRepository.findSimilar(
                    current.getCategory().getId(),
                    current.getBrand().getId(),
                    excluded,
                    PageRequest.of(0, Math.min(CATEGORY_BRAND_LIMIT, remaining)));
            addAll(picked, similar, excluded);
        }

        remaining = TARGET_SIZE - picked.size();
        if (remaining > 0) {
            List<Integer> bestSellerIds = orderItemRepository.findBestSellerProductIds(
                    OrderStatus.COMPLETED,
                    LocalDateTime.now().minusDays(BESTSELLER_DAYS),
                    excluded,
                    PageRequest.of(0, remaining));
            addAll(picked, loadOrdered(bestSellerIds), excluded);
        }

        remaining = TARGET_SIZE - picked.size();
        if (remaining > 0) {
            List<Product> onSale = productRepository.findOnSale(excluded, PageRequest.of(0, remaining));
            addAll(picked, onSale, excluded);
        }
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
