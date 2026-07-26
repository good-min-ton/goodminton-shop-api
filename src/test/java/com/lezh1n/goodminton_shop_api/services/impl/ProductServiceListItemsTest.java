package com.lezh1n.goodminton_shop_api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.Resources;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.ResourceRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceListItemsTest {

    @Mock ProductRepository productRepository;
    @Mock ResourceRepository resourceRepository;
    @Mock ProductMapper productMapper;

    @InjectMocks ProductServiceImpl service;

    private static Product product(int id) {
        return Product.builder().id(id).name("p" + id).slug("p" + id).build();
    }

    @Test
    void listItemsByIds_preservesRequestedOrder_dropsHidden_appliesBatchedThumbnail() {
        // Requested [1,2,3]; product 2 is hidden so the visible query omits it,
        // and returns the survivors in a DIFFERENT order (3 then 1).
        when(productRepository.findVisibleByIdInWithVariants(List.of(1, 2, 3)))
                .thenReturn(List.of(product(3), product(1)));
        when(resourceRepository.findByOwnerTypeAndOwnerIdInOrderBySortOrderAsc(
                eq(ResourceOwner.PRODUCT_THUMBNAIL), any()))
                .thenReturn(List.of(
                        Resources.builder().ownerId(1).url("http://t/1").sortOrder(0).build(),
                        Resources.builder().ownerId(1).url("http://t/1-b").sortOrder(1).build(),
                        Resources.builder().ownerId(3).url("http://t/3").sortOrder(0).build()));
        when(productMapper.toListItemResponse(any(Product.class), any()))
                .thenAnswer(inv -> ProductListItemResponse.builder()
                        .id(((Product) inv.getArgument(0)).getId())
                        .thumbnailUrl(inv.getArgument(1))
                        .build());

        List<ProductListItemResponse> result = service.listItemsByIds(List.of(1, 2, 3));

        // Order = requested order minus hidden(2): [1, 3]
        assertThat(result).extracting(ProductListItemResponse::getId).containsExactly(1, 3);
        // First row per owner (sort_order asc) is the thumbnail.
        assertThat(result.get(0).getThumbnailUrl()).isEqualTo("http://t/1");
        assertThat(result.get(1).getThumbnailUrl()).isEqualTo("http://t/3");
    }

    @Test
    void listItemsByIds_emptyInput_returnsEmpty() {
        assertThat(service.listItemsByIds(List.of())).isEmpty();
    }
}
