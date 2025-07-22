package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.VariantSize;

@Repository
public interface VariantSizeRepository extends JpaRepository<VariantSize, Integer> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM variant_size WHERE size_id = :sizeId)", nativeQuery = true)
    boolean existBySizeId(@Param("sizeId") Integer sizeId);
}
