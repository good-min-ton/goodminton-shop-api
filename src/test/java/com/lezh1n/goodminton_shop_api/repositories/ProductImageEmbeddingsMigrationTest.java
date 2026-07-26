package com.lezh1n.goodminton_shop_api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

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

import jakarta.persistence.EntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductImageEmbeddingsMigrationTest {

    @Autowired
    TestEntityManager em;

    private Integer productId;

    // Build a 768-dim pgvector literal with a single "hot" 1.0 component (rest 0.0).
    private static String vec(int hot) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 768; i++) {
            if (i > 0) sb.append(',');
            sb.append(i == hot ? "1" : "0");
        }
        return sb.append(']').toString();
    }

    private void insertEmbedding(int resourceId, String embedding) {
        EntityManager delegate = em.getEntityManager();
        delegate.createNativeQuery(
                "INSERT INTO product_image_embeddings(product_id, resource_id, url, embedding) "
                        + "VALUES (:pid, :rid, :url, CAST(:emb AS vector))")
                .setParameter("pid", productId)
                .setParameter("rid", resourceId)
                .setParameter("url", "http://img/" + resourceId)
                .setParameter("emb", embedding)
                .executeUpdate();
    }

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        Category category = em.persist(Category.builder().name("Rackets").build());
        Brand brand = em.persist(Brand.builder().name("Yonex").build());
        Product product = Product.builder()
                .category(category).brand(brand)
                .name("p").slug("p").isVisible(true)
                .createdAt(now).updatedAt(now)
                .variants(new ArrayList<>())
                .build();
        productId = em.persistAndFlush(product).getId();
    }

    @Test
    void cosineOrdering_returnsNearestFirst() {
        insertEmbedding(1, vec(0)); // aligned with the query vector -> distance 0
        insertEmbedding(2, vec(5)); // orthogonal -> distance 1
        em.flush();

        @SuppressWarnings("unchecked")
        List<Integer> ordered = em.getEntityManager().createNativeQuery(
                "SELECT resource_id FROM product_image_embeddings "
                        + "ORDER BY embedding <=> CAST(:q AS vector) ASC")
                .setParameter("q", vec(0))
                .getResultList();

        assertThat(ordered).containsExactly(1, 2);
    }

    @Test
    void deletingProduct_cascadeDeletesEmbeddings() {
        insertEmbedding(3, vec(0));
        em.flush();

        em.getEntityManager().createNativeQuery("DELETE FROM products WHERE id = :pid")
                .setParameter("pid", productId)
                .executeUpdate();
        em.flush();

        Number remaining = (Number) em.getEntityManager().createNativeQuery(
                "SELECT COUNT(*) FROM product_image_embeddings WHERE product_id = :pid")
                .setParameter("pid", productId)
                .getSingleResult();

        assertThat(remaining.longValue()).isZero();
    }
}
