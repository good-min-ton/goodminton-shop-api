package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.VariantImage;

@Repository
public interface VariantImageRepository extends JpaRepository<VariantImage, Integer> {
    List<VariantImage> findByVariantId(Integer variantId);
}
