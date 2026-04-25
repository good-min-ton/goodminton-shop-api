package com.lezh1n.goodminton_shop_api.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByCategory_Id(Integer categoryId);

    boolean existsByBrand_Id(Integer brandId);

    boolean existsBySlug(String slug);

    boolean existsByRelatedProduct_Id(Integer relatedProductId);

    @Query("SELECT p.id FROM Product p WHERE p.relatedProduct.id = :rootId")
    List<Integer> findIdsByRelatedProduct_Id(@Param("rootId") Integer rootId);

    @EntityGraph(attributePaths = {
            "category",
            "brand",
            "relatedProduct",
            "specifications",
            "variants",
            "variants.color",
            "variants.size"
    })
    @NonNull
    Optional<Product> findById(@NonNull Integer productId);

    @EntityGraph(attributePaths = { "variants" })
    @Query("""
            SELECT p FROM Product p
            WHERE p.id NOT IN :excluded
              AND p.category.id = :categoryId
              AND p.brand.id = :brandId
              AND p.isVisible = true
            ORDER BY p.createdAt DESC
            """)
    List<Product> findSimilar(@Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            @Param("excluded") Collection<Integer> excluded,
            Pageable pageable);

    @EntityGraph(attributePaths = { "variants" })
    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN p.variants pv
            WHERE pv.salePrice IS NOT NULL
              AND p.isVisible = true
              AND p.id NOT IN :excluded
            ORDER BY pv.updatedAt DESC
            """)
    List<Product> findOnSale(@Param("excluded") Collection<Integer> excluded, Pageable pageable);

    @EntityGraph(attributePaths = { "variants" })
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdInWithVariants(@Param("ids") Collection<Integer> ids);
}
