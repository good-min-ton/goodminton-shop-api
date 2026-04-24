package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    boolean existsByColor_Id(Integer colorId);

    boolean existsBySize_Id(Integer sizeId);

    boolean existsBySkuCode(String skuCode);

    List<ProductVariant> findByProduct_Id(Integer productId);

    void deleteByProduct_Id(Integer productId);

    Optional<ProductVariant> findByProduct_IdAndColor_IdAndSize_Id(
            Integer productId, Integer colorId, Integer sizeId);
}
