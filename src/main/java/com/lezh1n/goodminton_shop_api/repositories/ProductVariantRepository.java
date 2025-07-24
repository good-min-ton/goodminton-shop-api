package com.lezh1n.goodminton_shop_api.repositories;

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
}
