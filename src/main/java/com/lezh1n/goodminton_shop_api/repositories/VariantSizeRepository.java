package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.VariantSize;

@Repository
public interface VariantSizeRepository extends JpaRepository<VariantSize, Integer> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM variant_size WHERE size_id = :sizeId)", nativeQuery = true)
    boolean existBySizeId(@Param("sizeId") Integer sizeId);

    List<VariantSize> findByVariantVariantId(Integer variantId);

    @EntityGraph(attributePaths = {
            "variant.product",
            "variant.product.brand",
            "variant.version",
            "variant.color",
            "size"
    })
    @NonNull
    Optional<VariantSize> findById(@NonNull Integer variantSizeId);
}
