package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM product_variant WHERE version_id = :versionId)", nativeQuery = true)
    boolean existByVersionId(@Param("versionId") Integer versionId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM product_variant WHERE color_id = :colorId)", nativeQuery = true)
    boolean existByColorId(@Param("colorId") Integer colorId);

    @Query(value = "SELECT * FROM product_variant WHERE product_id = :productId", nativeQuery = true)
    List<ProductVariant> findAllVariantsOfProduct(@Param("productId") Integer productId);

    @Query(value = """
            SELECT DISTINCT v.name
            FROM product_variant pv
            JOIN version v ON pv.version_id = v.version_id
            WHERE pv.product_id = :productId
            AND pv.version_id IS NOT NULL
            """, nativeQuery = true)
    List<String> findAllVersionsOfProduct(@Param("productId") Integer productId);

    @Query(value = """
            SELECT DISTINCT c.name
            FROM product_variant pv
            JOIN color c ON pv.color_id = c.color_id
            WHERE pv.product_id = :productId
            AND pv.color_id IS NOT NULL
            """, nativeQuery = true)
    List<String> findAllColorsOfProduct(@Param("productId") Integer productId);

    @Query(value = """
            SELECT DISTINCT s.name
            FROM product_variant pv
            JOIN size s ON pv.size_id = c.size_id
            WHERE pv.product_id = :productId
            AND pv.size_id IS NOT NULL
            """, nativeQuery = true)
    List<String> findAllSizesOfProduct(@Param("productId") Integer productId);

    List<ProductVariant> findByProductId(Integer productId);
}
