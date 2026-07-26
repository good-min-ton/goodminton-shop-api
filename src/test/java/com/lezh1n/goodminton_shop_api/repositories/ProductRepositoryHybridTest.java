package com.lezh1n.goodminton_shop_api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.lezh1n.goodminton_shop_api.entities.Brand;
import com.lezh1n.goodminton_shop_api.entities.Category;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryHybridTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired
    ProductRepository productRepository;

    @Autowired
    TestEntityManager em;

    private Category category;
    private Brand brand;

    @BeforeEach
    void setUp() {
        category = em.persist(Category.builder().name("Rackets").build());
        brand = em.persist(Brand.builder().name("Yonex").build());
    }

    private Product persistProduct(String slug, boolean visible, BigDecimal salePrice) {
        Product p = Product.builder()
                .category(category).brand(brand)
                .name(slug).slug(slug).isVisible(visible)
                .createdAt(NOW).updatedAt(NOW)
                .variants(new ArrayList<>())
                .build();
        ProductVariant v = ProductVariant.builder()
                .product(p).skuCode(slug + "-sku")
                .price(new BigDecimal("100.00")).salePrice(salePrice)
                .updatedAt(NOW)
                .build();
        p.getVariants().add(v);
        return productRepository.saveAndFlush(p);
    }

    @Test
    void findIdsOnSaleIn_returnsOnlyVisibleOnSaleSubset() {
        Product onSale = persistProduct("on-sale", true, new BigDecimal("80.00"));
        Product notOnSale = persistProduct("full-price", true, null);
        Product hiddenOnSale = persistProduct("hidden-on-sale", false, new BigDecimal("70.00"));

        List<Integer> ids = List.of(onSale.getId(), notOnSale.getId(), hiddenOnSale.getId());

        assertThat(productRepository.findIdsOnSaleIn(ids))
                .containsExactly(onSale.getId());
    }

    @Test
    void findVisibleByIdInWithVariants_dropsHiddenAndLoadsVariants() {
        Product visible = persistProduct("visible", true, null);
        Product hidden = persistProduct("hidden", false, null);

        List<Product> result = productRepository.findVisibleByIdInWithVariants(
                List.of(visible.getId(), hidden.getId()));

        assertThat(result).extracting(Product::getId).containsExactly(visible.getId());
        assertThat(result.get(0).getVariants()).hasSize(1); // no LazyInitializationException
    }
}
