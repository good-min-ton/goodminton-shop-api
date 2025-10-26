package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.VariantImage;

@Repository
public interface VariantImageRepository extends JpaRepository<VariantImage, Integer> {
    List<VariantImage> findByVariantVariantId(Integer variantId);

    @Query("SELECT vi FROM VariantImage vi WHERE vi.sortOrder > :so")
    List<VariantImage> findImagesWithSortOrderGreaterThan(@Param("so") Integer sortOrder);

    VariantImage findTopByOrderBySortOrderDesc();
}
