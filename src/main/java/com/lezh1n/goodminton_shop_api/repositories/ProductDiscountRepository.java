package com.lezh1n.goodminton_shop_api.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.ProductDiscount;

@Repository
public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Integer> {
    @Query("""
            SELECT d
            FROM ProductDiscount d
            WHERE d.variantSize.variantSizeId = :variantSizeId
            AND d.startTime <= :now AND d.endTime >= :now
            """)
    Optional<ProductDiscount> findActiveDiscountByVariantSizeId(@Param("variantSizeId") Integer variantSizeId,
            @Param("now") LocalDateTime now);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM ProductDiscount d
            WHERE d.variantSize.variantSizeId = :variantSizeId
            AND d.startTime <= :endTime
            AND d.endTime >= :startTime
            """)
    boolean existByVariantSizeAndTime(
            @Param("variantSizeId") Integer variantSizeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
