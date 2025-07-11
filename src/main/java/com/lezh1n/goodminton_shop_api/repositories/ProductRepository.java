package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM product WHERE category_id = :categoryId)", nativeQuery = true)
    boolean existsByCategoryId(@Param("categoryId") Integer categoryId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM product WHERE brand_id = :brandId)", nativeQuery = true)
    boolean existsByBrandId(@Param("brandId") Integer brandId);
}
