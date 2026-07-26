package com.lezh1n.goodminton_shop_api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import com.lezh1n.goodminton_shop_api.client.RagCandidate;
import com.lezh1n.goodminton_shop_api.client.RagClient;
import com.lezh1n.goodminton_shop_api.configurations.CacheConfig;
import com.lezh1n.goodminton_shop_api.configurations.RagProperties;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.entities.Brand;
import com.lezh1n.goodminton_shop_api.entities.Category;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.services.RecommendationService;

@SpringBootTest
@Transactional
class RecommendationServiceIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @PersistenceContext
    EntityManager em;

    @Autowired
    RecommendationService recommendationService;

    @Autowired
    RagProperties ragProperties;

    @Autowired
    CacheManager cacheManager;

    @MockBean
    RagClient ragClient;

    private Category category;
    private Brand brand;

    @BeforeEach
    void setUp() {
        // Redis cache survives the DB rollback -> clear so each run recomputes.
        Cache cache = cacheManager.getCache(CacheConfig.RECOMMENDATIONS_CACHE);
        if (cache != null) {
            cache.clear();
        }
        category = Category.builder().name("Rackets").build();
        em.persist(category);
        brand = Brand.builder().name("Yonex").build();
        em.persist(brand);
    }

    private Product persist(String slug, boolean visible, LocalDateTime createdAt) {
        Product p = Product.builder()
                .category(category).brand(brand)
                .name(slug).slug(slug).isVisible(visible)
                .createdAt(createdAt).updatedAt(createdAt)
                .variants(new ArrayList<>())
                .build();
        ProductVariant v = ProductVariant.builder()
                .product(p).skuCode(slug + "-sku")
                .price(new BigDecimal("100.00")).salePrice(null)
                .updatedAt(createdAt)
                .build();
        p.getVariants().add(v);
        em.persist(p);
        return p;
    }

    @Test
    void hybrid_realRepositories_ordersSemanticThenFills_excludingFamily_toEight() {
        // Related family: root + current + sibling (whole family must be excluded).
        Product root = persist("root", true, NOW);
        Product current = persist("current", true, NOW);
        Product sibling = persist("sibling", true, NOW);
        current.setRelatedProduct(root);
        sibling.setRelatedProduct(root);

        // Semantic winners (visible) + one hidden candidate that must be dropped.
        Product a = persist("sem-a", true, NOW);
        Product b = persist("sem-b", true, NOW);
        Product c = persist("sem-c", true, NOW);
        Product hidden = persist("sem-hidden", false, NOW);

        // Fill pool: same category+brand, visible, no sale. createdAt DESC => h,g,f,e,d.
        Product d = persist("fill-d", true, NOW.minusDays(5));
        Product e = persist("fill-e", true, NOW.minusDays(4));
        Product f = persist("fill-f", true, NOW.minusDays(3));
        Product g = persist("fill-g", true, NOW.minusDays(2));
        Product h = persist("fill-h", true, NOW.minusDays(1));

        em.flush();
        em.clear(); // force the service's queries to hit the DB, not the 1st-level cache

        when(ragClient.similar(current.getId(), ragProperties.getRetrieveK()))
                .thenReturn(List.of(
                        new RagCandidate(a.getId(), 0.90),
                        new RagCandidate(root.getId(), 0.95),      // family -> excluded
                        new RagCandidate(b.getId(), 0.85),
                        new RagCandidate(sibling.getId(), 0.94),   // family -> excluded
                        new RagCandidate(hidden.getId(), 0.80),    // hidden -> dropped
                        new RagCandidate(c.getId(), 0.70)));

        List<Integer> ids = recommendationService.getRecommendations(current.getId())
                .stream().map(ProductListItemResponse::getId).toList();

        // Exactly 8 items filled to TARGET_SIZE.
        assertThat(ids).hasSize(8);
        // Semantic stage leads, in score order (no boosts -> pure similarity order).
        assertThat(ids.subList(0, 3)).containsExactly(a.getId(), b.getId(), c.getId());
        // Full membership: 3 semantic winners + 5 rule-based fill.
        assertThat(ids).containsExactlyInAnyOrder(
                a.getId(), b.getId(), c.getId(),
                d.getId(), e.getId(), f.getId(), g.getId(), h.getId());
        // Family + hidden are absent end-to-end.
        assertThat(ids).doesNotContain(
                current.getId(), root.getId(), sibling.getId(), hidden.getId());
    }
}
