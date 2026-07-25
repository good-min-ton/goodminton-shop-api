package com.lezh1n.goodminton_shop_api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.lezh1n.goodminton_shop_api.client.RagCandidate;
import com.lezh1n.goodminton_shop_api.client.RagClient;
import com.lezh1n.goodminton_shop_api.configurations.RagProperties;
import com.lezh1n.goodminton_shop_api.configurations.RecommendationProperties;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.entities.Brand;
import com.lezh1n.goodminton_shop_api.entities.Category;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.repositories.OrderItemRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.services.ResourceService;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock ProductRepository productRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock ProductMapper productMapper;
    @Mock ResourceService resourceService;
    @Mock RagClient ragClient;

    RagProperties ragProperties = new RagProperties();
    RecommendationProperties recommendationProperties = new RecommendationProperties();

    RecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        ragProperties.setRetrieveK(20);
        recommendationProperties.setBoostBestseller(0.03);
        recommendationProperties.setBoostSale(0.02);
        service = new RecommendationServiceImpl(
                productRepository, orderItemRepository, productMapper, resourceService,
                ragClient, ragProperties, recommendationProperties);

        lenient().when(resourceService.findSingle(any(), any())).thenReturn(Optional.empty());
        lenient().when(productMapper.toListItemResponse(any(Product.class), any()))
                .thenAnswer(inv -> {
                    Product p = inv.getArgument(0);
                    return ProductListItemResponse.builder().id(p.getId()).build();
                });
    }

    private Product product(int id) {
        Category cat = new Category();
        cat.setId(100);
        Brand brand = new Brand();
        brand.setId(200);
        Product p = new Product();
        p.setId(id);
        p.setCategory(cat);
        p.setBrand(brand);
        p.setIsVisible(true);
        return p;
    }

    private void stubCurrent() {
        when(productRepository.findById(3)).thenReturn(Optional.of(product(3)));
        when(productRepository.findIdsByRelatedProduct_Id(3)).thenReturn(List.of());
    }

    @Test
    void rerank_preservesSemanticOrder_whenNoBusinessSignals() {
        stubCurrent();
        when(ragClient.similar(3, 20)).thenReturn(List.of(
                new RagCandidate(10, 0.90),
                new RagCandidate(11, 0.89),
                new RagCandidate(12, 0.70)));
        when(productRepository.findVisibleByIdInWithVariants(any()))
                .thenReturn(List.of(product(10), product(11), product(12)));
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of());
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of());

        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(10, 11, 12);
    }

    @Test
    void rerank_boostOnlyOvertakesNearTie_notLargeGapLeader() {
        stubCurrent();
        when(ragClient.similar(3, 20)).thenReturn(List.of(
                new RagCandidate(10, 0.90),   // leader
                new RagCandidate(11, 0.89),   // near-tie, bestseller -> 0.92
                new RagCandidate(12, 0.70))); // large gap, bestseller -> 0.73
        when(productRepository.findVisibleByIdInWithVariants(any()))
                .thenReturn(List.of(product(10), product(11), product(12)));
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of());
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of(11, 12));

        // 11 (0.92) overtakes leader 10 (0.90); 12 (0.73) still ranks below 10 (0.90).
        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(11, 10, 12);
    }

    @Test
    void rerank_excludesSelfAndFamily() {
        when(productRepository.findById(3)).thenReturn(Optional.of(product(3)));
        when(productRepository.findIdsByRelatedProduct_Id(3)).thenReturn(List.of(4, 5));
        when(ragClient.similar(3, 20)).thenReturn(List.of(
                new RagCandidate(3, 0.99),    // self
                new RagCandidate(4, 0.98),    // sibling
                new RagCandidate(10, 0.80))); // valid
        when(productRepository.findVisibleByIdInWithVariants(any()))
                .thenReturn(List.of(product(10)));
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of());
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of());

        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(10);
    }

    @Test
    void rerank_dropsRagCandidateNotReturnedAsVisible() {
        stubCurrent();
        when(ragClient.similar(3, 20)).thenReturn(List.of(
                new RagCandidate(10, 0.90),
                new RagCandidate(11, 0.80)));  // now hidden/invisible
        // Visible load returns only 10 -> id 11 hits the `p == null` skip branch.
        when(productRepository.findVisibleByIdInWithVariants(any()))
                .thenReturn(List.of(product(10)));
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of());
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of());
        // Fill sources return nothing, so the endpoint still returns without error.
        when(productRepository.findSimilar(eq(100), eq(200), any(), any())).thenReturn(List.of());
        when(productRepository.findOnSale(any(), any())).thenReturn(List.of());

        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(10); // 11 dropped as not-visible
    }

    @Test
    void rerank_capsResultAtTargetSize() {
        stubCurrent();
        List<RagCandidate> ten = new ArrayList<>();
        List<Product> tenProducts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ten.add(new RagCandidate(100 + i, 0.99 - i * 0.01));
            tenProducts.add(product(100 + i));
        }
        when(ragClient.similar(3, 20)).thenReturn(ten);
        when(productRepository.findVisibleByIdInWithVariants(any())).thenReturn(tenProducts);
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of());
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of());

        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(100, 101, 102, 103, 104, 105, 106, 107);
    }

    @Test
    void emptyRag_fallsBackToRuleBasedPipeline() {
        stubCurrent();
        when(ragClient.similar(3, 20)).thenReturn(List.of());
        when(productRepository.findSimilar(eq(100), eq(200), any(), any()))
                .thenReturn(List.of(product(20), product(21), product(22), product(23), product(24)));
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of(30, 31, 32));
        when(productRepository.findAllByIdInWithVariants(any()))
                .thenReturn(List.of(product(30), product(31), product(32)));

        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(20, 21, 22, 23, 24, 30, 31, 32);
        verify(ragClient).similar(3, 20);
    }

    @Test
    void partialRag_topsUpToEightWithoutDuplicates() {
        stubCurrent();
        when(ragClient.similar(3, 20)).thenReturn(List.of(
                new RagCandidate(10, 0.9),
                new RagCandidate(11, 0.8),
                new RagCandidate(12, 0.7)));
        when(productRepository.findVisibleByIdInWithVariants(any()))
                .thenReturn(List.of(product(10), product(11), product(12)));
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of());
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of());
        // Fill: category+brand returns a duplicate (12) plus new ids.
        when(productRepository.findSimilar(eq(100), eq(200), any(), any()))
                .thenReturn(List.of(product(12), product(20), product(21), product(22), product(23)));
        when(productRepository.findOnSale(any(), any()))
                .thenReturn(List.of(product(24)));

        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(10, 11, 12, 20, 21, 22, 23, 24);
    }

    @Test
    void rerank_onSaleBoostOvertakesNearTie_notLargeGapLeader() {
        stubCurrent();
        when(ragClient.similar(3, 20)).thenReturn(List.of(
                new RagCandidate(10, 0.90),   // leader
                new RagCandidate(11, 0.89),   // near-tie, on-sale -> 0.91
                new RagCandidate(12, 0.70))); // large gap, on-sale -> 0.72
        when(productRepository.findVisibleByIdInWithVariants(any()))
                .thenReturn(List.of(product(10), product(11), product(12)));
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of(11, 12));
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of());

        // 11 (0.89 + 0.02 = 0.91) overtakes leader 10 (0.90);
        // 12 (0.70 + 0.02 = 0.72) still ranks below 10 (0.90) -> boost is small/semantic-dominant.
        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(11, 10, 12);
    }

    @Test
    void rerank_bestsellerAndOnSaleBoostsStack_appliesSum() {
        stubCurrent();
        when(ragClient.similar(3, 20)).thenReturn(List.of(
                new RagCandidate(10, 0.90),   // leader
                new RagCandidate(11, 0.86),   // both bestseller + on-sale -> 0.86 + 0.05 = 0.91
                new RagCandidate(12, 0.70))); // no signals
        when(productRepository.findVisibleByIdInWithVariants(any()))
                .thenReturn(List.of(product(10), product(11), product(12)));
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of(11));
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of(11));

        // Only the STACKED +0.05 (0.03 + 0.02) lifts 11 to 0.91 past leader 10 (0.90);
        // either boost alone (0.86 + 0.03 = 0.89 or 0.86 + 0.02 = 0.88) would stay below 0.90.
        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                .containsExactly(11, 10, 12);
    }

    @Test
    void fillCategoryBrand_isBoundedByRemaining_andCapsAtEight() {
        stubCurrent();
        // Semantic stage yields exactly 6 (no business signals) -> remaining == 2 for the fill.
        List<RagCandidate> six = new ArrayList<>();
        List<Product> sixProducts = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            six.add(new RagCandidate(10 + i, 0.99 - i * 0.01));
            sixProducts.add(product(10 + i));
        }
        when(ragClient.similar(3, 20)).thenReturn(six);
        when(productRepository.findVisibleByIdInWithVariants(any())).thenReturn(sixProducts);
        when(productRepository.findIdsOnSaleIn(any())).thenReturn(List.of());
        when(orderItemRepository.findBestSellerProductIds(eq(OrderStatus.COMPLETED), any(), any(), any()))
                .thenReturn(List.of());
        // Fill source returns MORE than remaining (5): three already-picked dups + two new ids.
        when(productRepository.findSimilar(eq(100), eq(200), any(), any()))
                .thenReturn(List.of(product(10), product(11), product(12), product(20), product(21)));

        assertThat(service.getRecommendations(3))
                .extracting(ProductListItemResponse::getId)
                // dups (10,11,12) dropped, only 20 & 21 added -> exactly 8, no duplicates.
                .containsExactly(10, 11, 12, 13, 14, 15, 20, 21);

        // The Math.min(CATEGORY_BRAND_LIMIT=5, remaining=2) guard must request only 2 from the DB,
        // not the unconditional 5 -> this page-size is the guard's sole observable effect (the mock
        // ignores paging, so a broken guard would silently over-fetch in production).
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findSimilar(eq(100), eq(200), any(), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(2);
    }

    @Test
    void getRecommendations_throwsWhenProductMissing() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRecommendations(999))
                .isInstanceOf(AppException.class);
        verifyNoInteractions(ragClient);
    }
}
